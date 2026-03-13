import useAuth from '@/auth/store';
import { Spinner } from '@/components/ui/spinner';
import { refreshToken } from '@/services/AuthService';
import React, { useEffect, useState } from 'react'
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router';

/*
 * OAuthSuccess Component
 *
 * This page is used after a successful OAuth login (Google/GitHub).
 * When the backend completes OAuth authentication, it redirects the
 * user to this frontend route.
 *
 * The backend has already set the refresh token in a secure cookie.
 * This component now calls the refreshToken API to obtain an access token
 * and store the user information in the global auth state.
 */
function OAuthSuccess() {

    // Tracks whether the refresh API is currently being called
    const [isRefreshing, setIsRefreshing] = useState<boolean>(false);

    /*
     * Zustand global auth store method used to update
     * accessToken, user, and authStatus locally.
     */
    const changeLocalLoginData = useAuth((state) => state.changeLocalLoginData);

    // React Router navigation hook used to redirect user
    const navigate = useNavigate();


    /*
     * useEffect runs when the component mounts.
     * It calls the refreshToken API to obtain a fresh access token.
     */
    useEffect(() => {

        async function getAccessToken() {

            // Prevent duplicate API calls
            if (!isRefreshing) {

                setIsRefreshing(true);

                try {

                    /*
                     * refreshToken API reads the refresh token from the cookie
                     * and requests a new access token from the backend.
                     */
                    const responseLoginData = await refreshToken();

                    /*
                     * Update global authentication state with
                     * the new access token and user details.
                     */
                    changeLocalLoginData(
                        responseLoginData.accessToken,
                        responseLoginData.user,
                        true
                    );

                    toast.success("Login successful!!");

                    // Redirect user to the dashboard after successful login
                    navigate("/dashboard");

                } catch (error) {

                    toast.error("Login failed!!");
                    console.log(error);

                } finally {

                    setIsRefreshing(false);
                }
            }
        }

        getAccessToken();

    }, []);


    /*
     * While the refresh request is processing,
     * show a loading indicator to the user.
     */
    return (
        <div className='p-10 flex justify-center items-center'>
            <Spinner />
            <h1>Please wait...</h1>
        </div>
    )
}

export default OAuthSuccess;