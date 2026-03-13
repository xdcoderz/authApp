import useAuth from '@/auth/store'
import React from 'react'
import { Navigate, Outlet } from 'react-router'

/*
 * UserLayout
 *
 * This component acts as a protected route wrapper.
 * It ensures that only authenticated users can access
 * certain parts of the application (for example the dashboard).
 *
 * If the user is authenticated → render child routes.
 * If not authenticated → redirect to the login page.
 */
function Userlayout() {

  // Access the authentication check helper from the global auth store
  const checkLogin = useAuth(state => state.checkLogin);

  /*
   * If the user is logged in, render nested routes using <Outlet/>.
   * Outlet is a React Router component that renders the child
   * route defined inside this layout.
   */
  if (checkLogin())
    return (
      <div>
        <Outlet />
      </div>
    );

  /*
   * If the user is not authenticated, redirect them to the login page.
   * Navigate component performs programmatic navigation in React Router.
   */
  else
    return <Navigate to={"/login"} />
}

export default Userlayout