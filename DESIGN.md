---
name: Modern Campus Pulse
colors:
  surface: '#111319'
  surface-dim: '#111319'
  surface-bright: '#363940'
  surface-container-lowest: '#0b0e14'
  surface-container-low: '#191c22'
  surface-container: '#1d2026'
  surface-container-high: '#272a30'
  surface-container-highest: '#32353b'
  on-surface: '#e1e2eb'
  on-surface-variant: '#c2c6d4'
  inverse-surface: '#e1e2eb'
  inverse-on-surface: '#2e3037'
  outline: '#8c919e'
  outline-variant: '#424752'
  surface-tint: '#aac7ff'
  primary: '#aac7ff'
  on-primary: '#002f65'
  primary-container: '#5d9cff'
  on-primary-container: '#00336b'
  inverse-primary: '#005cb9'
  secondary: '#7adaa1'
  on-secondary: '#003920'
  secondary-container: '#007848'
  on-secondary-container: '#9bfcc1'
  tertiary: '#c5c0ff'
  on-tertiary: '#2a1f7e'
  tertiary-container: '#9891f3'
  on-tertiary-container: '#2e2482'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d6e3ff'
  primary-fixed-dim: '#aac7ff'
  on-primary-fixed: '#001b3e'
  on-primary-fixed-variant: '#00458e'
  secondary-fixed: '#95f7bb'
  secondary-fixed-dim: '#7adaa1'
  on-secondary-fixed: '#002110'
  on-secondary-fixed-variant: '#005230'
  tertiary-fixed: '#e3dfff'
  tertiary-fixed-dim: '#c5c0ff'
  on-tertiary-fixed: '#140067'
  on-tertiary-fixed-variant: '#413996'
  background: '#111319'
  on-background: '#e1e2eb'
  surface-variant: '#32353b'
typography:
  headline-xl:
    fontFamily: Work Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-lg:
    fontFamily: Work Sans
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
  headline-md:
    fontFamily: Work Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Work Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Work Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Work Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Work Sans
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-margin: 16px
  stack-gap-lg: 24px
  stack-gap-md: 16px
  stack-gap-sm: 8px
  inline-padding: 12px
---

## Brand & Style

This design system is built for a fast-paced, information-heavy mobile environment where clarity and speed of recognition are paramount. The aesthetic is rooted in **Modern Corporate** principles but injected with high-energy accents to appeal to a younger, tech-savvy demographic. 

The brand personality is efficient, reliable, and vibrant. It avoids the heaviness often associated with dark themes by using a sophisticated dark gray palette rather than pure black, ensuring depth and readability. Visual interest is maintained through high-contrast highlights and a modular card-based architecture that makes dense data feel digestible and organized.

## Colors

The color palette is centered on a "Deep Charcoal" ecosystem to reduce eye strain while providing a premium backdrop for content. 

*   **Primary (Electric Blue):** Used for primary actions, active states, and critical information highlights.
*   **Secondary (Vibrant Green):** Reserved for success states, environmental/map markers, and positive data trends.
*   **Surface Colors:** We utilize a tiered gray system. The base background is the darkest, while cards and interactive elements sit on a slightly lighter surface to create a sense of physical layering.
*   **Functional Grays:** Text follows a strict hierarchy of white (High Emphasis), medium gray (Medium Emphasis), and dark gray (Disabled/Placeholder).

## Typography

This design system utilizes **Work Sans** across all levels to maintain a professional, neutral tone that excels in high-density data environments. 

The type scale is optimized for mobile legibility. Headlines are bold and tight to anchor distinct sections, while labels use slightly increased letter spacing to remain readable at small scales against dark backgrounds. For numeric data and dates, the "tabular-nums" OpenType feature should be enabled to ensure alignment in list views and schedules.

## Layout & Spacing

The layout follows a **Fluid Grid** model with a focus on vertical stackability. 

*   **Margins:** A standard 16px margin is applied to the left and right of the main screen container.
*   **Sectioning:** Each major content block (News, Maps, Schedule) is separated by a 24px vertical gap to provide clear visual breathing room.
*   **Internal Padding:** Cards use a 12px or 16px internal padding depending on content density. 
*   **Horizontal Rhythm:** For carousel elements (like news banners or club icons), the first item must align with the container margin, with subsequent items "peeking" from the right edge to indicate scrollability.

## Elevation & Depth

In this dark-themed environment, depth is communicated through **Tonal Layers** rather than heavy shadows. 

1.  **Level 0 (Background):** The deepest layer (#121214), used for the main canvas.
2.  **Level 1 (Cards/Containers):** Elevated surfaces (#212226). These use a very subtle 1px inner stroke (#34363C) to define edges against the background.
3.  **Level 2 (Interactive Elements):** Buttons and active chips. These use high-saturation color fills or higher-contrast grays.

Shadows, if used, are extremely subtle (opacity < 20%) and serve only to lift the highest-priority floating elements, like a floating action button or a modal sheet.

## Shapes

The design system employs a **Rounded** shape language to soften the industrial feel of the dark palette and make the interface feel more approachable.

*   **Cards:** Use a 16px (1rem) corner radius to create a distinct, modern container look.
*   **Buttons & Chips:** Use a pill-shaped (fully rounded) approach for category tags and primary buttons to differentiate them from the rectangular card structures.
*   **Media/Maps:** Internal media elements within cards should match the 8px or 12px radius of their parent containers to maintain nested harmony.

## Components

### Buttons & Chips
*   **Primary Action:** Solid Primary Blue fill with white text.
*   **Secondary Action:** Surface Gray fill with a 1px border.
*   **Category Chips:** Pill-shaped. Active state uses a light gray or primary blue; inactive state uses a low-contrast dark gray surface.

### Cards
*   **Information Cards:** Feature a title, subtitle, and often a leading accent bar (e.g., a 4px blue vertical line on the left) to indicate category.
*   **Hero Banners:** Utilize full-bleed imagery or vibrant gradient backgrounds with overlayed text.
*   **Map Modules:** Integrated cards with a fixed aspect ratio, featuring rounded corners and a "view all" text link in the header.

### Lists
*   **News/Announcement Lists:** Items are separated by thin dividers or contained within a unified card with internal dividers. Dates are right-aligned in a secondary text color.

### Navigation
*   **Bottom Bar:** A persistent surface with line-based icons. The active state is indicated by a color shift to the Primary Blue and a subtle weight increase in the icon or text label.