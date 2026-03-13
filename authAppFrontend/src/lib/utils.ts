import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

/*
 * cn (className utility)
 *
 * Helper function used to combine and clean Tailwind CSS class names.
 * It merges multiple class strings while resolving Tailwind conflicts.
 *
 * Two libraries are used here:
 *
 * clsx:
 * - Allows conditional class names.
 * - Example: clsx("btn", isActive && "btn-active")
 *
 * tailwind-merge:
 * - Removes conflicting Tailwind classes.
 * - Example: "p-2 p-4" → "p-4"
 *
 * Together they ensure that dynamic class names remain clean
 * and that Tailwind styles do not conflict.
 */
export function cn(...inputs: ClassValue[]) {

  // clsx combines the class values
  // twMerge resolves Tailwind conflicts
  return twMerge(clsx(inputs))
}