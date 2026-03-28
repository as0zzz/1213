# Minecraft 1.16.5 Custom Render Cheat Client — Technical Specification

## 1. Overview

This document defines the architecture and requirements for a Minecraft 1.16.5 cheat client with a **fully custom rendering pipeline**, independent from Minecraft's default rendering systems.

The key goal is to ensure that **all visual features (ESP, HUD, particles, GUI, effects)** are rendered using a **custom OpenGL-based renderer (LWJGL)** rather than Minecraft’s built-in systems (`RenderSystem`, `Tessellator`, etc.).

---

## 2. Core Principles

- ❌ Do NOT rely on Minecraft rendering utilities for visual modules  
- ✅ Use raw OpenGL (LWJGL) for all rendering  
- ✅ Fully decouple logic and rendering  
- ✅ Ensure high performance and extensibility  

---

## 3. Rendering Engine

### 3.1 General

- Implement a **custom rendering engine** using LWJGL OpenGL bindings
- Hook into Minecraft render loop (e.g. via `GameRenderer` or `WorldRenderer` injection)
- Maintain independent render pipeline

### 3.2 Features

- 2D rendering system (HUD, GUI)
- 3D rendering system (ESP, world overlays)
- Custom matrix stack abstraction
- Depth control (enable/disable for overlays)
- Alpha blending
- Scissor/clipping system

### 3.3 Camera Synchronization

- Sync camera with player:
  - Position (x, y, z)
  - Rotation (yaw, pitch)
- Implement world-to-screen projection:
  - Custom projection matrix
  - Frustum culling support

---

## 4. ESP System

### 4.1 Features

- Box ESP (2D & 3D)
- Health bar (dynamic scaling)
- Name tags (custom font rendering)
- Tracers (lines from player to entity)
- Optional glow/shader effects

### 4.2 Requirements

- Fully rendered via custom renderer
- Smooth interpolation of entity positions
- Configurable colors and styles
- Optional visibility checks

---

## 5. Particle System

### 5.1 General

- Fully independent particle engine
- No usage of Minecraft particle system

### 5.2 Triggers

- Entity hit
- Movement
- Jump
- Custom events

### 5.3 Particle Properties

- Velocity (x, y, z)
- Size
- Color (RGBA)
- Lifetime
- Optional gravity

### 5.4 Behavior

- Smooth animation
- Fade-out over time
- Optional physics simulation
- Batched rendering

---

## 6. HUD System

### 6.1 Components

- ArrayList (active modules)
- Watermark
- FPS counter
- Ping display

### 6.2 Features

- Fully custom rendering
- Gradient color support
- Dynamic layout
- Drag & drop positioning

### 6.3 Animations

- Fade-in/out
- Slide transitions
- Easing-based animations

---

## 7. ClickGUI

### 7.1 General

- Fully custom GUI system
- No Minecraft GUI components

### 7.2 Features

- Categories (Combat, Visual, Movement, etc.)
- Module toggles
- Sliders, checkboxes, dropdowns
- Scrollable panels

### 7.3 Visual Effects

- Blur (framebuffer-based)
- Shadows
- Rounded corners
- Smooth transitions

---

## 8. Animation System

### 8.1 Easing Functions

- Linear
- Cubic
- Quint
- Ease-in / Ease-out / Ease-in-out

### 8.2 Interpolation

- LERP (linear interpolation)
- Optional smooth damp

### 8.3 Usage

- GUI animations
- HUD transitions
- ESP smoothing
- Particle motion

---

## 9. Architecture

### 9.1 Module System

- Base `Module` class
- `ModuleManager`:
  - Module registration
  - Enable/disable logic
  - Categorization

### 9.2 Event System

- Event bus pattern
- Core events:
  - RenderEvent (2D / 3D)
  - TickEvent
  - InputEvent
  - WorldEvent

### 9.3 Separation of Concerns

- Logic layer (modules)
- Render layer (graphics)
- Data layer (settings/configs)

---

## 10. Optimization

### 10.1 Rendering

- Batch draw calls
- Minimize OpenGL state changes
- Use VBOs where possible

### 10.2 Data

- Cache entity data per frame
- Avoid redundant calculations

### 10.3 Performance Goals

- Minimal FPS impact
- Stable frame timing
- Scalable with many entities

---

## 11. Shader Support

### 11.1 Features

- Custom GLSL shaders
- Glow effects
- Blur effects
- Gradient rendering

### 11.2 Pipeline

- Framebuffer usage
- Post-processing passes

---

## 12. Theming System

- RGB and gradient themes
- Runtime switching
- Centralized style manager

---

## 13. Extensibility

- Simple module API
- Expandable rendering system
- Optional plugin system

---

## 14. Suggested Project Structure
client/
├── core/
│ ├── Client.java
│ ├── ModuleManager.java
│ ├── EventBus.java
│
├── modules/
│ ├── visual/
│ │ ├── ESP.java
│ │ ├── HUD.java
│ │ ├── Particles.java
│
├── render/
│ ├── Renderer.java
│ ├── Render2D.java
│ ├── Render3D.java
│ ├── Shader.java
│
├── gui/
│ ├── ClickGUI.java
│ ├── components/
│
├── utils/
│ ├── MathUtils.java
│ ├── AnimationUtils.java
│
├── shaders/
│ ├── glow.glsl
│ ├── blur.glsl
---

## 15. Future Improvements

- Multi-pass shader pipeline
- GPU instancing
- Advanced ESP (skeletons, hitboxes)
- JSON config system
- Script support (Lua / JS)

---

## 16. Summary

The client must provide:

- Full control over rendering
- High-performance visuals
- Clean modular architecture
- Advanced customization

**Key principle:** Complete independence from Minecraft's rendering pipeline.