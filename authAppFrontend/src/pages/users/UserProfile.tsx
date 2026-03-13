import { useState, useRef } from "react";
import { motion } from "framer-motion";
import {
  Mail,
  Shield,
  Calendar,
  Clock,
  Camera,
  Github,
  Pencil,
  Save,
  X,
  CheckCircle,
  XCircle,
} from "lucide-react";

import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import useAuth from "@/auth/store";

/*
 * GoogleIcon
 *
 * Small SVG icon used to visually represent the Google OAuth provider.
 * This is shown when a user logged in using Google authentication.
 */
function GoogleIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 48 48" {...props}>
      <path
        fill="#FFC107"
        d="M43.6 20.5H42V20H24v8h11.3C33.7 32.6 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.2 8 3.2l5.7-5.7C34.1 6.1 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 19-8.9 19-20c0-1.1-.1-2.3-.4-3.5z"
      />
    </svg>
  );
}

/*
 * UserProfile Component
 *
 * Displays the profile page for the currently authenticated user.
 * The data is pulled from the global auth store (Zustand).
 *
 * Features:
 * - View profile information
 * - Edit name and avatar locally
 * - Shows authentication provider
 * - Displays account metadata like creation time
 */
export default function UserProfile() {

  // Get current logged-in user from global auth store
  const user = useAuth((state) => state.user);

  // Controls whether the profile is in edit mode
  const [editMode, setEditMode] = useState(false);

  // Reference for hidden file input (used for avatar upload)
  const fileRef = useRef<HTMLInputElement>(null);

  /*
   * Local form state for editable fields.
   * Initially filled with values from the user object.
   */
  const [form, setForm] = useState({
    name: user?.name ?? "",
    image: user?.image ?? "",
  });

  /*
   * Handles changes in text inputs (e.g. name field)
   */
  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  /*
   * Handles avatar image change.
   * Currently creates a temporary preview URL.
   * In a real system this would upload the file to the backend.
   */
  const onAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setForm({ ...form, image: URL.createObjectURL(file) });
  };

  /*
   * Cancel editing and restore original user data.
   */
  const cancelEdit = () => {
    setEditMode(false);
    setForm({
      name: user?.name ?? "",
      image: user?.image ?? "",
    });
  };

  /*
   * Save profile changes.
   *
   * TODO:
   * This should call a backend API like:
   * PATCH /api/v1/users/me
   */
  const saveChanges = () => {
    setEditMode(false);
    // 🔜 Backend PATCH /users/me
  };

  // Prevent rendering if user is not available
  if (!user) return null;

  return (
    <div className="min-h-screen bg-background px-6 py-10">

      {/* Page Header */}
      <div className="mb-10 flex justify-between items-center">

        <div>
          <h1 className="text-3xl font-bold">User Profile</h1>
          <p className="text-muted-foreground">
            Manage your account information
          </p>
        </div>

        {/* Toggle edit mode */}
        {!editMode ? (
          <Button onClick={() => setEditMode(true)} className="gap-2">
            <Pencil size={16} /> Edit Profile
          </Button>
        ) : (
          <div className="flex gap-2">

            <Button variant="outline" onClick={cancelEdit}>
              <X size={16} /> Cancel
            </Button>

            <Button onClick={saveChanges}>
              <Save size={16} /> Save
            </Button>

          </div>
        )}
      </div>


      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

        {/* Profile Card */}
        <Card className="rounded-3xl">

          <CardContent className="p-8 flex flex-col items-center gap-6">

            {/* Avatar */}
            <div className="relative group">

              <Avatar className="h-32 w-32">

                <AvatarImage src={form.image} />

                <AvatarFallback>
                  {user.name?.charAt(0) ?? "U"}
                </AvatarFallback>

              </Avatar>

              {/* Avatar upload overlay (visible in edit mode) */}
              {editMode && (
                <button
                  onClick={() => fileRef.current?.click()}
                  className="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
                >
                  <Camera className="text-white" />
                </button>
              )}

              <input
                ref={fileRef}
                hidden
                type="file"
                accept="image/*"
                onChange={onAvatarChange}
              />

            </div>

            {/* Editable name field */}
            {editMode ? (
              <Input
                name="name"
                value={form.name}
                onChange={onChange}
                placeholder="Your name"
              />
            ) : (
              <h2 className="text-xl font-semibold">
                {user.name || "Unnamed User"}
              </h2>
            )}

            {/* Email */}
            <p className="text-sm text-muted-foreground">
              {user.email}
            </p>

          </CardContent>
        </Card>


        {/* Details Section */}
        <div className="lg:col-span-2 space-y-6">

          {/* Account Information */}
          <Section title="Account Information">

            <StaticRow
              icon={<Mail />}
              label="Email"
              value={user.email}
            />

            <StaticRow
              icon={<Shield />}
              label="Provider"
              value={user.provider}
              extra={
                user.provider === "GOOGLE" ? (
                  <GoogleIcon className="h-5 w-5" />
                ) : user.provider === "GITHUB" ? (
                  <Github className="h-5 w-5" />
                ) : (
                  "LOCAL"
                )
              }
            />

            <StaticRow
              icon={user.enabled ? <CheckCircle /> : <XCircle />}
              label="Account Status"
              value={user.enabled ? "Enabled" : "Disabled"}
            />

          </Section>


          {/* Activity Metadata */}
          <Section title="Activity">

            <StaticRow
              icon={<Calendar />}
              label="Account Created"
              value={
                user.createdAt
                  ? new Date(user.createdAt).toLocaleDateString()
                  : "—"
              }
            />

            <StaticRow
              icon={<Clock />}
              label="Last Updated"
              value={
                user.updatedAt
                  ? new Date(user.updatedAt).toLocaleDateString()
                  : "—"
              }
            />

          </Section>

        </div>
      </div>
    </div>
  );
}


/*
 * Section Component
 *
 * Reusable wrapper for grouped profile information.
 */
function Section({ title, children }: any) {
  return (
    <Card>
      <CardContent className="p-6 space-y-4">
        <h3 className="font-semibold text-lg">{title}</h3>
        {children}
      </CardContent>
    </Card>
  );
}


/*
 * StaticRow Component
 *
 * Displays a single labeled field inside profile sections.
 * Used for read-only information like email or provider.
 */
function StaticRow({ icon, label, value, extra }: any) {
  return (
    <div className="flex items-center justify-between">

      <div className="flex gap-4 items-center">

        <div className="h-10 w-10 rounded-xl bg-primary/15 flex items-center justify-center">
          {icon}
        </div>

        <div>
          <p className="text-sm text-muted-foreground">
            {label}
          </p>

          <p className="font-medium">
            {value}
          </p>
        </div>

      </div>

      {/* Optional extra element (icon, badge, etc.) */}
      {extra}

    </div>
  );
}