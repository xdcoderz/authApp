import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { User, Mail, Lock } from "lucide-react";
import { motion } from "framer-motion";
import toast from "react-hot-toast";
import { useState, type FormEvent } from "react";
import type RegisterData from "@/models/RegisterData";
import { registeruser } from "@/services/AuthService";
import { useNavigate } from "react-router";
import OAuth2Buttons from "@/components/OAuth2Buttons";

/*
 * Signup Component
 *
 * This page handles new user registration.
 * It collects the user's name, email, and password,
 * performs basic validation, and sends the data
 * to the backend registration API.
 *
 * After successful registration, the user is redirected
 * to the login page.
 */
export default function Signup() {

  /*
   * Local form state storing user input.
   * Matches the RegisterData model used by the backend.
   */
  const [data, setData] = useState<RegisterData>({
    name: '',
    email: '',
    password: ''
  });

  // Loading state used while API request is processing
  const [loading, setLoading] = useState<boolean>(false);

  // Stores possible errors returned by server
  const [error, setError] = useState(null);

  // Navigation hook from React Router
  const navigate = useNavigate();


  /*
   * handleInputChange
   *
   * Updates the form state whenever the user
   * types inside an input field.
   */
  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {

    setData((value) => ({
      ...value,
      [event.target.name]: event.target.value,
    }));

  };


  /*
   * handleFormSubmit
   *
   * Handles the registration form submission.
   * Steps:
   * 1. Prevent page reload
   * 2. Validate inputs
   * 3. Send registration request to backend
   * 4. Redirect user to login page after success
   */
  const handleFormSubmit = async (event: FormEvent) => {

    event.preventDefault();

    // Basic validation checks
    if (data.name.trim() === "") {
      toast.error("Name is required!");
      return;
    }

    if (data.email.trim() === "") {
      toast.error("Email is required!");
      return;
    }

    if (data.password.trim() === "") {
      toast.error("Password is required!");
      return;
    }

    try {

      setLoading(true);

      // Call backend API to register the user
      const result = await registeruser(data);

      console.log(result);

      toast.success("User Registered Successfully");

      // Reset form after successful registration
      setData({
        name: "",
        email: "",
        password: ""
      });

      // Redirect user to login page
      navigate("/login");

    } catch (error) {

      console.log(error);

      toast.error("Error in registering the user");

    } finally {

      setLoading(false);

    }

  };


  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground relative overflow-hidden px-4">

      {/* Background glow effect */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_hsl(var(--primary)/0.35),_transparent_60%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom,_hsl(var(--secondary)/0.25),_transparent_65%)]" />
      </div>

      {/* Animated container */}
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
                Create Your Account
              </h1>

              <p className="mt-3 text-muted-foreground">
                Get started with secure, modern authentication
              </p>

            </div>


            {/* Signup Form */}
            <form onSubmit={handleFormSubmit} className="space-y-6">

              {/* Name Field */}
              <div className="space-y-2">

                <Label htmlFor="name">Full Name</Label>

                <div className="relative">

                  <User className="absolute left-3 top-3 h-5 w-5 text-muted-foreground" />

                  <Input
                    id="name"
                    type="text"
                    placeholder="John Doe"
                    className="pl-10"
                    name="name"
                    value={data.name}
                    onChange={handleInputChange}
                  />

                </div>

              </div>


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
                    value={data.email}
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
                    value={data.password}
                    onChange={handleInputChange}
                  />

                </div>

              </div>


              {/* Submit Button */}
              <Button
                disabled={loading}
                className="w-full rounded-full text-lg py-6"
              >
                {loading ? "Creating account..." : "Create Account"}
              </Button>

            </form>


            {/* Divider between signup and OAuth */}
            <div className="my-8 flex items-center gap-4">

              <div className="h-px flex-1 bg-border" />

              <span className="text-xs text-muted-foreground">
                OR
              </span>

              <div className="h-px flex-1 bg-border" />

            </div>


            {/* OAuth login buttons */}
            <OAuth2Buttons />


            {/* Footer */}
            <p className="mt-10 text-center text-sm text-muted-foreground">

              Already have an account?{" "}

              <a href="#" className="text-primary hover:underline">
                Sign in
              </a>

            </p>

          </CardContent>

        </Card>

      </motion.div>

    </div>
  );
}