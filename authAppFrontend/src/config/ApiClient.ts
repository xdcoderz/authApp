import axios from 'axios';
import useAuth from '@/auth/store';
import { refreshToken } from '@/services/AuthService';

/*
 * apiClient
 *
 * Central Axios instance used for making API requests to the backend.
 * Instead of creating axios calls everywhere in the app, we configure
 * a single reusable client.
 *
 * Features configured here:
 * - Base API URL
 * - JSON headers
 * - Cookies enabled (required for refresh token cookie)
 * - Request interceptor (adds access token automatically)
 * - Response interceptor (handles token refresh when access token expires)
 */
const apiClient = axios.create({
    // Base URL of backend API
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api/v1',

    // Default headers for requests
    headers: {
        'Content-Type': "application/json",
    },

    // Allows cookies (refresh token cookie) to be sent with requests
    withCredentials: true,

    // Request timeout
    timeout: 10000
});


/*
 * REQUEST INTERCEPTOR
 *
 * Runs before every API request.
 * If an access token exists in the global auth store,
 * it is automatically attached to the Authorization header.
 */
apiClient.interceptors.request.use(config => {

    const accessToken = useAuth.getState().accessToken;

    if (accessToken) {
        config.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return config;
});


/*
 * Token refresh management variables
 *
 * isRefreshing → prevents multiple refresh calls at the same time
 * pending      → stores requests waiting for a new token
 */
let isRefreshing = false;
let pending: any[] = [];


/*
 * queueRequest
 *
 * Stores API requests that failed due to expired access token.
 * These requests will be retried once a new token is received.
 */
function queueRequest(cb: any) {
    pending.push(cb);
}


/*
 * resolveQueue
 *
 * Once a new access token is obtained, all queued requests
 * are retried using the new token.
 */
function resolveQueue(newToken: string) {
    pending.forEach(cb => cb(newToken));
    pending = [];
}


/*
 * RESPONSE INTERCEPTOR
 *
 * Handles API errors globally.
 * Specifically intercepts 401 Unauthorized errors and
 * attempts to refresh the access token automatically.
 */
apiClient.interceptors.response.use(

    // Successful responses pass through normally
    (response) => response,

    async (error) => {

        console.log(error);

        const is401 = error.response && error.response.status === 401;
        const original = error.config;

        console.log("original retry: ", original._retry);

        // If the error is not 401 OR request already retried → reject
        if (!is401 || original._retry) {
            return Promise.reject(error);
        }

        original._retry = true;

        /*
         * If another refresh request is already running,
         * queue the current request until the new token arrives.
         */
        if (isRefreshing) {

            console.log("added to queue....");

            return new Promise((resolve, reject) => {

                queueRequest((newToken: string) => {

                    if (!newToken) return reject();

                    original.headers.Authorization = `Bearer ${newToken}`;

                    resolve(apiClient(original));
                });

            });
        }


        /*
         * Start refresh token process
         */
        isRefreshing = true;

        try {

            console.log("start refreshing...");

            // Call refresh token API
            const loginResponse = await refreshToken();

            const newToken = loginResponse.accessToken;

            if (!newToken)
                throw new Error("no access token received");


            /*
             * Update global authentication state
             * with new access token and user data.
             */
            useAuth
                .getState()
                .changeLocalLoginData(
                    loginResponse.accessToken,
                    loginResponse.user,
                    true
                );


            // Retry queued requests with the new token
            resolveQueue(newToken);

            // Retry the original request
            original.headers.Authorization = `Bearer ${newToken}`;

            return apiClient(original);

        } catch (error) {

            /*
             * If refresh fails:
             * - reject all queued requests
             * - log the user out
             */
            resolveQueue('null');

            useAuth.getState().logout();

            return Promise.reject(error);

        } finally {

            isRefreshing = false;
        }
    }
);

export default apiClient;