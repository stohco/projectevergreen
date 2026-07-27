import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { Toaster } from "@/components/ui/toaster";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Er Gen Verse · 仙逆 · Renegade Immortal",
  description: "A AAA Three.js voxel xianxia open world. Faithful adaptation of Er Gen's Renegade Immortal (仙逆). Explore Planet Suzaku, cultivate to transcendence, walk the Heaven Trampling bridges.",
  keywords: ["仙逆", "Renegade Immortal", "Er Gen", "xianxia", "cultivation", "Three.js", "voxel", "open world"],
  authors: [{ name: "Er Gen Verse" }],
  icons: {
    icon: "https://z-cdn.chatglm.cn/z-ai/static/logo.svg",
  },
  openGraph: {
    title: "Er Gen Verse · 仙逆",
    description: "A AAA voxel xianxia open world — Renegade Immortal adaptation",
    url: "https://chat.z.ai",
    siteName: "Er Gen Verse",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Er Gen Verse · 仙逆",
    description: "A AAA voxel xianxia open world — Renegade Immortal adaptation",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-background text-foreground`}
      >
        {children}
        <Toaster />
      </body>
    </html>
  );
}
