import type RegisterData from "@/models/RegisterData"
import apiClient from "@/config/ApiClient";
import type LoginData from "@/models/LoginData";
import type LoginResponseData from "@/models/LoginResponseData";
import type User from "@/models/User";

/*
 * AuthService
 *
 * This file contains all authentication-related API calls used by the frontend.
 * It acts as a central service layer between the UI components and the backend.
 *
 * Instead of calling axios directly inside components, we keep API logic here.
 * This makes the code cleaner, reusable, and easier to maintain.
 */


/*
 * registeruser
 *
 * Sends user registration data to the backend.
 * Endpoint: POST /auth/register
 *
 * Used in: Signup page
 *
 * The backend will:
 * - Validate user data
 * - Create a new user record
 * - Store it in the database
 */
export const registeruser = async (signupData: RegisterData) => {

    const response = await apiClient.post(`/auth/register`, signupData);

    // Return only the response body (actual data)
    return response.data;
};


/*
 * loginUser
 *
 * Sends login credentials to the backend authentication API.
 * Endpoint: POST /auth/login
 *
 * Used in:
 * - Login page
 * - Auth store (zustand)
 *
 * Backend returns:
 * - accessToken
 * - refreshToken
 * - user details
 */
export const loginUser = async (loginData: LoginData) => {

    const response = await apiClient.post<LoginResponseData>(
        "/auth/login",
        loginData
    );

    return response.data;
};


/*
 * logoutUser
 *
 * Logs the user out of the system.
 * Endpoint: POST /auth/logout
 *
 * Backend responsibilities:
 * - Revoke refresh token
 * - Clear authentication cookie
 *
 * Used in:
 * - Navbar logout button
 * - Auth store logout action
 */
export const logoutUser = async () => {

    const response = await apiClient.post("/auth/logout");

    return response.data;
};


/*
 * getCurrentUser
 *
 * Fetches user information using email.
 * Endpoint: GET /users/email/{email}
 *
 * Used in:
 * - Dashboard page
 * - Profile section
 *
 * Returns full user details from backend.
 */
export const getCurrentUser = async (emailId: string | undefined) => {

    const response = await apiClient.get<User>(
        `/users/email/${emailId}`
    );

    return response.data;
};


/*
 * refreshToken
 *
 * Requests a new access token using the refresh token.
 * Endpoint: POST /auth/refresh
 *
 * The refresh token is stored in a secure HTTP-only cookie,
 * so it is automatically sent with the request.
 *
 * Used in:
 * - Axios interceptor
 * - OAuthSuccess page
 *
 * Backend returns:
 * - new access token
 * - refreshed user data
 */
export const refreshToken = async () => {

    const response = await apiClient.post<LoginResponseData>(
        `auth/refresh`
    );

    return response.data;
};