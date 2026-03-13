import React from 'react'
import { Button } from './ui/button'
import { NavLink } from 'react-router'
import useAuth from '@/auth/store'

/*
 * Navbar Component
 *
 * Navigation bar displayed across the application.
 * It shows different navigation options depending on whether
 * the user is authenticated or not.
 *
 * The authentication state is obtained from the global Zustand
 * auth store (useAuth).
 */
function Navbar() {

    // Helper function to check if the user is logged in
    const checkLogin = useAuth((state) => state.checkLogin);

    // Logged-in user's information
    const user = useAuth((state) => state.user);

    // Logout action from auth store
    const logout = useAuth((state) => state.logout);

    return (
        <nav className='py-5 border-b border-gray-400 md:py-0 flex md:flex-row gap-4 md:gap-0 flex-col md:h-14 justify-around items-center'>

            {/* Application brand / logo */}
            <div className="font-semibold items-center flex gap-2">

                {/* Small brand icon */}
                <span className='inline-block text-center h-6 w-6 rounded-md bg-gradient-to-br from-primary to-primary/40'>
                    A
                </span>

                {/* Application name */}
                <span className='text-base tracking-light'>
                    Auth App
                </span>
            </div>


            {/* Navigation links */}
            <div className='flex gap-2 items-center'>

                {
                    /*
                     * If user is logged in:
                     * show profile link and logout button
                     */
                    checkLogin() ? (
                        <>
                            {/* Profile page link showing user's name */}
                            <NavLink to='/dashboard/profile'>
                                {user?.name}
                            </NavLink>

                            {/* Logout button clears auth state */}
                            <Button
                                onClick={() => logout()}
                                size={'sm'}
                                className='cursor-pointer'
                                variant={'outline'}
                            >
                                Logout
                            </Button>
                        </>
                    ) : (

                        /*
                         * If user is NOT logged in:
                         * show public navigation links
                         */
                        <>
                            {/* Home page */}
                            <NavLink to='/'>
                                Home
                            </NavLink>

                            {/* Login page */}
                            <NavLink to={'/login'}>
                                <Button
                                    size={'sm'}
                                    className='cursor-pointer'
                                    variant={'outline'}
                                >
                                    Login
                                </Button>
                            </NavLink>

                            {/* Signup page */}
                            <NavLink to={'/signup'}>
                                <Button
                                    size={'sm'}
                                    className='cursor-pointer'
                                    variant={'outline'}
                                >
                                    Signup
                                </Button>
                            </NavLink>
                        </>
                    )
                }

            </div>

        </nav>
    )
}

export default Navbar