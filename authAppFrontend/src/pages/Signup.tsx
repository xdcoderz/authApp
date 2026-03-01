import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { User, Mail, Lock, Github } from "lucide-react";
import { motion } from "framer-motion";
import  toast, { Toaster } from "react-hot-toast";
import { useState, type FormEvent } from "react";
import type RegisterData from "@/models/RegisterData";
import { registeruser } from "@/services/AuthService";
import { useNavigate } from "react-router";


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



export default function Signup() {

  const [data, setData] = useState<RegisterData>({
    name:'',
    email:'',
    password:''
  });

  const [loading, setLoading] =  useState<boolean>(false);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  //to bind the data 

  //text input, email, password, number, textarea
  //this function is this for handling form input change
  const handleInputChange = (event:React.ChangeEvent<HTMLInputElement>) => {
      // console.log(event.target.name)
      // console.log(event.target.value)
      setData((value) => ({
        ...value,
        [event.target.name]: event.target.value,
      }));
  };

  //handling form submit 
  const handleFormSubmit = async(event:React.FormEvent) =>{
    event.preventDefault();
    console.log(data);


    //validations 
    if(data.name.trim () === ""){
      toast.error("Name is required ! ");
      return;
    }

    if(data.email.trim () === ""){
      toast.error("Email is required ! ");
      return;
    }

    if(data.password.trim () === ""){
      toast.error("Password is required ! ");
      return;
    }

    //form submit for registration 
    try{

      const result = await registeruser(data);
      console.log(result);
      toast.success("User Regsiter Successfully...")
      setData({
        name:"",
        email:"",
        password:""
      });
      //navigates to login page 
      navigate("/login");
    } catch (error){
      console.log(error);
      toast.error("Error in registering the user...")
    }

  };


  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground relative overflow-hidden px-4">
      {/* Background Glow */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_hsl(var(--primary)/0.35),_transparent_60%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom,_hsl(var(--secondary)/0.25),_transparent_65%)]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7, ease: "easeOut" }}
        className="w-full max-w-md"
      >
        <Card className="rounded-3xl border bg-background/70 backdrop-blur-xl shadow-2xl">
          <CardContent className="p-10">
            {/* Heading */}
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

              <Button className="w-full rounded-full text-lg py-6">
                Create Account
              </Button>
            </form>

            {/* Divider */}
            <div className="my-8 flex items-center gap-4">
              <div className="h-px flex-1 bg-border" />
              <span className="text-xs text-muted-foreground">OR</span>
              <div className="h-px flex-1 bg-border" />
            </div>

            {/* OAuth */}
            <div className="space-y-4">
              <Button variant="outline" className="w-full rounded-full py-6">
                <div className="flex items-center justify-center gap-3">
                  <GoogleIcon className="h-5 w-5" />
                  <span>Sign up with Google</span>
                </div>
              </Button>

              <Button variant="outline" className="w-full rounded-full py-6">
                <div className="flex items-center justify-center gap-3">
                  <Github className="h-5 w-5" />
                  <span>Sign up with GitHub</span>
                </div>
              </Button>
            </div>

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
