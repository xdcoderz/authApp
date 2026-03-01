import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Github, ShieldCheck, Clock, User } from "lucide-react";
import useAuth from "@/auth/store";
import { motion } from "framer-motion";
import { getCurrentUser } from "@/services/AuthService";
import { useState } from "react";
import type UserT from "@/models/User";
import toast from "react-hot-toast";

/* Reuse Google icon (same as Login page) */
function GoogleIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 48 48" {...props}>
      <path
        fill="#FFC107"
        d="M43.6 20.5H42V20H24v8h11.3C33.7 32.6 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.2 8 3.2l5.7-5.7C34.1 6.1 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 19-8.9 19-20c0-1.1-.1-2.3-.4-3.5z"
      />
      <path
        fill="#FF3D00"
        d="M6.3 14.7l6.6 4.8C14.6 16.1 19 12 24 12c3.1 0 5.9 1.2 8 3.2l5.7-5.7C34.1 6.1 29.3 4 24 4 16.3 4 9.6 8.3 6.3 14.7z"
      />
      <path
        fill="#4CAF50"
        d="M24 44c5.2 0 10-2 13.6-5.3l-6.3-5.2C29.3 35.5 26.7 36 24 36c-5.3 0-9.7-3.4-11.3-8.1l-6.5 5C9.4 39.7 16.2 44 24 44z"
      />
      <path
        fill="#1976D2"
        d="M43.6 20.5H42V20H24v8h11.3c-1.1 3-3.4 5.5-6.3 6.8l6.3 5.2C39.3 36.4 43 30.9 43 24c0-1.1-.1-2.3-.4-3.5z"
      />
    </svg>
  );
}

export default function Userhome() {

  const user = useAuth((state) => state.user);
  const [user1, setUser1] = useState<UserT | null>(null);
  const getUserData = async() => {
    try {
      const user1  = await getCurrentUser(user?.email)
      setUser1(user1);
      toast.success("User data fetched successfully")
    } catch (error) {
      console.log(error)
      toast.error("Failed to fetch user data")
    }
  }


  return (
    <div className="min-h-screen bg-background text-foreground px-6 py-10">
      {/* Page Header */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="mb-10"
      >
        <h1 className="text-3xl font-extrabold tracking-tight">
          Welcome, {user?.name ?? "User"} 👋
        </h1>
        <p className="text-muted-foreground mt-2">
          This is your secure dashboard overview
        </p>
      </motion.div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <StatCard
          icon={<User />}
          title="Account Type"
          value={user?.provider ?? "LOCAL"}
        />
        <StatCard
          icon={<ShieldCheck />}
          title="Account Status"
          value={user?.enable ? "Active" : "Disabled"}
        />
        <StatCard
          icon={<Clock />}
          title="Session"
          value="Valid"
        />
      </div>

      {/* OAuth Providers */}
      <Card className="rounded-3xl bg-background/60 backdrop-blur-xl border shadow-xl">
        <CardContent className="p-8 space-y-6">
          <h2 className="text-xl font-semibold">Authentication Providers</h2>

          <div className="flex flex-wrap gap-6">
            <OAuthBadge
              label="Local"
              active={user?.provider === "LOCAL"}
              icon={
                <div className="h-9 w-9 rounded-full bg-gradient-to-br from-primary to-primary/40 flex items-center justify-center font-bold">
                  L
                </div>
              }
            />

            <OAuthBadge
              label="Google"
              active={user?.provider === "GOOGLE"}
              icon={<GoogleIcon className="h-8 w-8" />}
            />

            <OAuthBadge
              label="GitHub"
              active={user?.provider === "GITHUB"}
              icon={<Github className="h-8 w-8" />}
            />
          </div>

          <p className="text-sm text-muted-foreground">
            Logged in using{" "}
            <span className="font-medium text-foreground">
              {user?.provider ?? "LOCAL"}
            </span>
          </p>
        </CardContent>
      </Card>

      <Card className="rounded-3xl bg-background/60 backdrop-blur-xl border shadow-xl">
        <CardContent className="p-8 space-y-6">
          <Button onClick = {getUserData} className = "rounded-2xl px-8 text-lg">Get current user</Button>
          <p>{user1 ? `User: ${user1.name}` : "No user data available"}</p>
          </CardContent>
          </Card>

      {/* Dummy Action */}
      <div className="mt-10 flex gap-4">
        <Button variant="outline" className="rounded-full">
          View Profile
        </Button>
        <Button className="rounded-full">
          Manage Security
        </Button>
      </div>
    </div>
  );
}

/* ------------------- Components ------------------- */

function StatCard({
  icon,
  title,
  value,
}: {
  icon: React.ReactNode;
  title: string;
  value: string;
}) {
  return (
    <Card className="rounded-3xl bg-background/60 backdrop-blur-xl border shadow-lg">
      <CardContent className="p-6 flex items-center gap-4">
        <div className="h-12 w-12 rounded-xl bg-primary/15 text-primary flex items-center justify-center">
          {icon}
        </div>
        <div>
          <p className="text-sm text-muted-foreground">{title}</p>
          <p className="text-lg font-semibold">{value}</p>
        </div>
      </CardContent>
    </Card>
  );
}

function OAuthBadge({
  icon,
  label,
  active,
}: {
  icon: React.ReactNode;
  label: string;
  active: boolean;
}) {
  return (
    <div
      className={`flex flex-col items-center gap-2 px-5 py-4 rounded-2xl border transition-all
      ${
        active
          ? "bg-primary/10 border-primary shadow-[0_0_30px_hsl(var(--primary)/0.35)]"
          : "bg-background/40 border-border"
      }`}
    >
      {icon}
      <span className="text-sm">{label}</span>
      {active && (
        <span className="text-[10px] uppercase tracking-wide text-primary">
          Active
        </span>
      )}
    </div>
  );
}