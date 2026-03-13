import React from 'react'
import { Button } from './ui/button'
import { Github } from 'lucide-react'
import { NavLink } from 'react-router';

function GoogleIcon(props) {
  return (
    <svg viewBox="0 0 48 48" {...props}>
      <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.6 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.2 8 3.2l5.7-5.7C34.1 6.1 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 19-8.9 19-20c0-1.1-.1-2.3-.4-3.5z"/>
      <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.6 16.1 19 12 24 12c3.1 0 5.9 1.2 8 3.2l5.7-5.7C34.1 6.1 29.3 4 24 4 16.3 4 9.6 8.3 6.3 14.7z"/>
      <path fill="#4CAF50" d="M24 44c5.2 0 10-2 13.6-5.3l-6.3-5.2C29.3 35.5 26.7 36 24 36c-5.3 0-9.7-3.4-11.3-8.1l-6.5 5C9.4 39.7 16.2 44 24 44z"/>
      <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-1.1 3-3.4 5.5-6.3 6.8l6.3 5.2C39.3 36.4 43 30.9 43 24c0-1.1-.1-2.3-.4-3.5z"/>
    </svg>
  );
}

function OAuth2Buttons() {
  return (
    <div className="space-y-4">
        <NavLink to = {`${import.meta.env.VITE_BASE_URL || 'http://localhost:8083'}/oauth2/authorization/google`} className={"block"}>
            <Button
                type="button"
                variant="outline"
                className="w-full rounded-full py-6 text-base cursor-pointer"
              >
                <div className="flex items-center justify-center gap-3">
                  <GoogleIcon className="h-5 w-5" />
                  <span>Continue with Google</span>
                </div>
              </Button>
 
        </NavLink>
        <NavLink to = {`${import.meta.env.VITE_BASE_URL || 'http://localhost:8083'}/oauth2/authorization/github`} className={"block"}>
            <Button
                type="button"
                variant="outline"
                className="w-full rounded-full py-6 text-base cursor-pointer"
              >
                <div className="flex items-center justify-center gap-3">
                  <Github className="h-5 w-5" />
                  <span>Continue with GitHub</span>
                </div>
              </Button>
        </NavLink>

              
        </div>
  )
}

export default OAuth2Buttons
