import type User from '@/models/User';
import { create } from 'zustand';
import type LoginData from '@/models/LoginData';
import { loginUser, logoutUser } from '@/services/AuthService';
import type LoginResponseData from '@/models/LoginResponseData';
import { persist } from 'zustand/middleware';

/*
 * LOCAL_KEY
 *
 * Key used by Zustand persist middleware to store authentication
 * state in the browser's localStorage. This allows login state
 * to survive page refreshes.
 */
const LOCAL_KEY = 'auth_state';

/*
 * AuthState
 *
 * Type definition for the global authentication state.
 * Zustand uses this to enforce the structure of the store.
 *
 * This state is used across the React application to track:
 * - logged in user
 * - access token
 * - authentication status
 * - login/logout actions
 */
type AuthState = {
    accessToken: string | null;   // JWT access token used for API requests
    user: User | null;            // currently logged-in user
    authStatus: boolean;          // true if user is authenticated
    authLoading: boolean;         // indicates login/logout request in progress

    // login function that calls backend authentication API
    login: (loginData: LoginData) => Promise<LoginResponseData>;

    // logout function to clear session and tokens
    logout: (silent?: boolean) => void;

    // helper method used to quickly check login status
    checkLogin: () => boolean | undefined;

    // utility method to manually update auth state
    changeLocalLoginData: (
        accessToken: string,
        user: User,
        authStatus: boolean
    ) => void;
};


/*
 * useAuth
 *
 * Zustand store used as a global authentication state manager.
 *
 * Zustand is a lightweight state management library for React
 * similar to Redux but much simpler.
 *
 * persist() middleware automatically saves the auth state
 * into localStorage and restores it when the app reloads.
 */
const useAuth = create<AuthState>()(
    persist(
        (set, get) => ({

            // initial state
            accessToken: null,
            user: null,
            authStatus: false,
            authLoading: false,


            /*
             * changeLocalLoginData
             *
             * Utility function used to directly update
             * authentication data inside the store.
             *
             * Useful when login happens from another flow
             * (for example OAuth redirect handling).
             */
            changeLocalLoginData: (accessToken, user, authStatus) => {
                set({
                    accessToken,
                    user,
                    authStatus
                });
            },


            /*
             * login
             *
             * Performs login by calling the backend authentication API.
             *
             * Steps:
             * 1. Set loading state
             * 2. Call loginUser API
             * 3. Save returned token and user in global store
             * 4. Return login response
             */
            login: async (loginData) => {

                console.log("started login...");
                set({ authLoading: true });

                try {

                    const loginResponseData = await loginUser(loginData);
                    console.log(loginResponseData);

                    // Update authentication state after successful login
                    set({
                        accessToken: loginResponseData.accessToken,
                        user: loginResponseData.user,
                        authStatus: true,
                    });

                    return loginResponseData;

                } catch (error) {

                    console.log("error", error);
                    throw error;

                } finally {

                    // stop loading indicator
                    set({
                        authLoading: false,
                    });
                }
            },


            /*
             * logout
             *
             * Logs the user out of the application.
             *
             * It optionally calls the backend logout API
             * to revoke refresh tokens before clearing local state.
             */
            logout: async (silent = false) => {

                try {

                    set({
                        authLoading: true,
                    });

                    // call backend logout endpoint
                    await logoutUser();

                } catch (error) {
                    // logout errors are ignored to ensure client state clears

                } finally {

                    set({
                        authLoading: false,
                    });
                }

                // Clear authentication state locally
                set({
                    accessToken: null,
                    user: null,
                    authStatus: false,
                    authLoading: false,
                });
            },


            /*
             * checkLogin
             *
             * Helper function used by components to check
             * whether a user is currently authenticated.
             */
            checkLogin: () => {

                if (get().accessToken && get().authStatus)
                    return true;
                else
                    return false;
            },

        }),

        /*
         * persist configuration
         *
         * This tells Zustand to save the authentication state
         * inside localStorage using the provided key.
         */
        {
            name: LOCAL_KEY,
        }
    )
);

export default useAuth;