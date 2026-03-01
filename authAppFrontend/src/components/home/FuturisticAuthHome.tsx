import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ShieldCheck, Lock, Zap, Users, Fingerprint, Globe, Moon, Sun } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";

export default function HomePage() {
  const [theme, setTheme] = useState("dark");

  useEffect(() => {
    document.documentElement.classList.toggle("dark", theme === "dark");
  }, [theme]);

  return (
    <div className="min-h-screen bg-background text-foreground transition-colors duration-700">
      {/* Theme Toggle */}
      <div className="fixed top-6 right-6 z-50">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          className="rounded-full backdrop-blur bg-background/60 border"
        >
          {theme === "dark" ? <Sun /> : <Moon />}
        </Button>
      </div>

      {/* HERO */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 -z-10">
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_hsl(var(--primary)/0.35),_transparent_60%)]" />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom,_hsl(var(--secondary)/0.25),_transparent_65%)]" />
        </div>

        <div className="max-w-7xl mx-auto px-6 py-32 text-center">
          <motion.h1
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
            className="text-6xl md:text-7xl font-extrabold tracking-tight"
          >
            Authentication
            <span className="block bg-gradient-to-r from-primary to-cyan-400 bg-clip-text text-transparent">
              Designed for the Future
            </span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="mt-8 text-xl text-muted-foreground max-w-2xl mx-auto"
          >
            Passwordless. Zero-Trust. Beautiful by default.
            Build secure authentication experiences users actually enjoy.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="mt-12 flex justify-center gap-6"
          >
            <Button size="lg" className="rounded-full px-10 text-lg">
              Get Started Free
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="rounded-full px-10 text-lg"
            >
              View Documentation
            </Button>
          </motion.div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="max-w-7xl mx-auto px-6 py-28">
        <h2 className="text-4xl font-bold text-center mb-20">
          Everything You Need for Secure Access
        </h2>

        <div className="grid md:grid-cols-3 gap-10">
          {features.map((f, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.08 }}
            >
              <Card className="group rounded-3xl border bg-background/70 backdrop-blur-xl shadow-xl hover:shadow-2xl transition-all">
                <CardContent className="p-10">
                  <div className="h-14 w-14 rounded-2xl bg-primary/10 flex items-center justify-center mb-6 group-hover:scale-110 transition">
                    <f.icon className="h-7 w-7 text-primary" />
                  </div>
                  <h3 className="text-2xl font-semibold mb-4">{f.title}</h3>
                  <p className="text-muted-foreground text-lg">{f.desc}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section className="border-y bg-muted/30">
        <div className="max-w-7xl mx-auto px-6 py-28">
          <h2 className="text-4xl font-bold text-center mb-20">
            Simple. Secure. Scalable.
          </h2>

          <div className="grid md:grid-cols-3 gap-16">
            <Step number="01" title="Integrate" desc="SDKs & APIs ready for frontend and backend." />
            <Step number="02" title="Authenticate" desc="Biometrics, OTP, magic links, and MFA." />
            <Step number="03" title="Authorize" desc="Zero-trust checks with real-time risk scoring." />
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_center,_hsl(var(--primary)/0.4),_transparent_65%)]" />
        <div className="max-w-5xl mx-auto px-6 py-32 text-center">
          <h2 className="text-5xl font-extrabold">
            Ship Secure Auth in Minutes
          </h2>
          <p className="mt-8 text-xl text-muted-foreground">
            Trusted by developers who care about security, speed, and design.
          </p>
          <div className="mt-12">
            <Button size="lg" className="rounded-full px-14 py-7 text-xl">
              Create Your Free Account
            </Button>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t">
        <div className="max-w-7xl mx-auto px-6 py-12 flex flex-col md:flex-row justify-between items-center gap-6">
          <p className="text-muted-foreground">© 2026 AuthX</p>
          <div className="flex gap-8 text-muted-foreground">
            <a className="hover:text-foreground">Docs</a>
            <a className="hover:text-foreground">Security</a>
            <a className="hover:text-foreground">Contact</a>
          </div>
        </div>
      </footer>
    </div>
  );
}

const features = [
  {
    title: "Zero-Trust Security",
    desc: "Every request verified using contextual and behavioral signals.",
    icon: ShieldCheck,
  },
  {
    title: "Passwordless Login",
    desc: "Biometrics, magic links, and OTP-based authentication flows.",
    icon: Fingerprint,
  },
  {
    title: "Multi-Factor Auth",
    desc: "Add extra protection layers without harming user experience.",
    icon: Lock,
  },
  {
    title: "Blazing Fast",
    desc: "Edge-optimized infrastructure for instant authentication.",
    icon: Zap,
  },
  {
    title: "User & Session Control",
    desc: "Full visibility into sessions, roles, and devices.",
    icon: Users,
  },
  {
    title: "Global Ready",
    desc: "Compliance-ready, multi-region, enterprise-grade by default.",
    icon: Globe,
  },
];

function Step({ number, title, desc }) {
  return (
    <div className="text-center space-y-6">
      <div className="mx-auto h-14 w-14 rounded-full bg-primary/10 flex items-center justify-center text-primary text-2xl font-bold">
        {number}
      </div>
      <h3 className="text-2xl font-semibold">{title}</h3>
      <p className="text-muted-foreground text-lg">{desc}</p>
    </div>
  );
}
