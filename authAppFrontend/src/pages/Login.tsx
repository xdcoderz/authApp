import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Mail, Lock, Github, CheckCircle2Icon } from "lucide-react";
import { motion } from "framer-motion";
import { useState, type FormEvent } from "react";
import type LoginData from "@/models/LoginData";
import { useNavigate } from "react-router";
import toast from "react-hot-toast";
import { Alert, AlertTitle } from "@/components/ui/alert";
import { Spinner } from "@/components/ui/spinner";
import useAuth from "@/auth/store";
import OAuth2Buttons from "@/components/OAuth2Buttons";

/*
 * Login Component
 *
 * This page handles user authentication using email and password.
 * It connects the login form with the global auth state (Zustand store).
 *
 * Main responsibilities:
 * - Collect login credentials
 * - Validate inputs
 * - Call backend login API through auth store
 * - Handle loading and error states
 * - Redirect user to dashboard after successful login
 */
export default function Login() {

  /*
   * Local state storing login form data.
   * This object matches the LoginData model used by the backend API.
   */
  const [loginData, setLoginData] = useState<LoginData>({
    email: "",
    password: "",
  });

  // Indicates whether login request is currently processing
  const [loading, setLoading] = useState<boolean>(false);

  // Stores error returned from server
  const [error, setError] = useState<any>(null);

  // React Router navigation hook
  const navigate = useNavigate();

  /*
   * Access login function from global auth store.
   * This function internally calls the login API
   * and updates the global authentication state.
   */
  const login = useAuth((state) => state.login);

  /*
   * handleInputChange
   *
   * Updates form state whenever user types in
   * email or password input fields.
   */
  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {

    setLoginData({
      ...loginData,
      [event.target.name]: event.target.value
    });

  };

  /*
   * handleFormSubmit
   *
   * Triggered when the login form is submitted.
   *
   * Steps:
   * 1. Prevent default form submission
   * 2. Validate user inputs
   * 3. Call login API through auth store
   * 4. Redirect user after success
   */
  const handleFormSubmit = async (event: FormEvent) => {

    event.preventDefault();

    // Basic input validation
    if (loginData.email.trim() === '') {
      toast.error("Email required!");
      return;
    }

    if (loginData.password.trim() === '') {
      toast.error("Password required!");
      return;
    }

    try {

      setLoading(true);

      // Call login function from auth store
      await login(loginData);

      toast.success("Login Success");

      // Redirect to dashboard after successful login
      navigate('/dashboard');

    } catch (error: any) {

      console.log(error);

      setError(error);

      toast.error("Error in Login!");

      // Display server error message
      if (error?.status == 400) {
        setError(error);
      } else {
        setError(error);
      }

    } finally {

      setLoading(false);

    }

  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground relative overflow-hidden px-4">

      {/* Decorative background glow effect */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_hsl(var(--primary)/0.35),_transparent_60%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom,_hsl(var(--secondary)/0.25),_transparent_65%)]" />
      </div>

      {/* Animated login card container */}
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7, ease: "easeOut" }}
        className="w-full max-w-md"
      >

        <Card className="rounded-3xl border bg-background/70 backdrop-blur-xl shadow-2xl">

          <CardContent className="p-10">

            {/* Page Heading */}
            <div className="text-center mb-10">

              <h1 className="text-3xl font-extrabold tracking-tight">
                Welcome Back
              </h1>

              <p className="mt-3 text-muted-foreground">
                Login in to continue to your secure app
              </p>

            </div>

            {/* Error Message Section */}
            {error && (
              <div className="mb-4">
                <Alert variant={"destructive"}>
                  <CheckCircle2Icon />

                  <AlertTitle>
                    {error?.response
                      ? error?.response?.data?.message
                      : error?.message}
                  </AlertTitle>

                </Alert>
              </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleFormSubmit} className="space-y-6">

              {/* Email Field */}
              <div className="space-y-2">

                <Label htmlFor="email">Email</Label>

                <div className="relative">

                  <Mail className="absolute left-3 top-3 h-5 w-5 text-muted-foreground" />

                  <Input
                    id="email"
                    type="email"
                    placeholder="you@example.com"
                    className="pl-10"
                    name="email"
                    value={loginData.email}
                    onChange={handleInputChange}
                  />

                </div>

              </div>


              {/* Password Field */}
              <div className="space-y-2">

                <Label htmlFor="password">Password</Label>

                <div className="relative">

                  <Lock className="absolute left-3 top-3 h-5 w-5 text-muted-foreground" />

                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    className="pl-10"
                    name="password"
                    value={loginData.password}
                    onChange={handleInputChange}
                  />

                </div>

              </div>


              {/* Forgot password link */}
              <div className="flex items-center justify-between text-sm">

                <a href="#" className="text-primary hover:underline">
                  Forgot password?
                </a>

              </div>


              {/* Submit Button */}
              <Button
                disabled={loading}
                className="w-full cursor-pointer rounded-full text-lg py-6"
              >

                {loading
                  ? (
                    <>
                      <Spinner />
                      Please wait...
                    </>
                  )
                  : "Login"
                }

              </Button>

            </form>


            {/* Divider between login and OAuth */}
            <div className="my-8 flex items-center gap-4">

              <div className="h-px flex-1 bg-border" />

              <span className="text-xs text-muted-foreground">
                OR
              </span>

              <div className="h-px flex-1 bg-border" />

            </div>


            {/* OAuth login buttons (Google / GitHub) */}
            <OAuth2Buttons />


            {/* Signup Footer */}
            <p className="mt-10 text-center text-sm text-muted-foreground">

              Don’t have an account?{" "}

              <a href="#" className="text-primary hover:underline">
                Sign up
              </a>

            </p>

          </CardContent>

        </Card>

      </motion.div>

    </div>
  );
}