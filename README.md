# RecallRhythm 🎵🧠

[![Java CI](https://github.com/dweepagrawal25-bit/Recall-Rhythm/actions/workflows/build.yml/badge.svg)](https://github.com/dweepagrawal25-bit/Recall-Rhythm/actions)
![Java](https://img.shields.io/badge/Java-11%20%7C%2017%20%7C%2021-ED8B00?logo=openjdk&logoColor=white)
![Swing GUI](https://img.shields.io/badge/GUI-Java%20Swing%20%26%20AWT-blue)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**RecallRhythm** is a modern, intuitive Spaced Revision & Flashcard Planner desktop application built in Java Swing. It optimizes long-term memory retention using spaced repetition scheduling algorithms to help students and developers master concepts, formulas, and flashcards efficiently.

---

## ✨ Features

- **🧠 Enhanced Spaced Repetition (4-Level Leitner / SM-2)**:
  - 🟥 **Again (1 / Left)**: Resets card to Box 1 for immediate/next-day reinforcement.
  - 🟧 **Hard (2 / Down)**: Conservative interval keeping card in current box.
  - 🟩 **Good (3 / Right)**: Standard Leitner progression (+1 box level).
  - 🟦 **Easy (4 / Up)**: Accelerated mastery leap (+2 box levels).
  - ⚡ **Cram Mode**: Practice all cards on demand even when none are due.
- **🌙 Dynamic Dark & Light Themes**:
  - Full toggleable Dark Mode (rich Slate palette) and clean Light Mode.
  - Consistent theming across cards, charts, forms, and dialogs.
- **🔊 Procedural Audio Feedback**:
  - Built-in zero-dependency MIDI sound engine (`SoundEffect.java`) providing auditory cues for flips, ratings, and celebratory session completion. Includes mute toggle.
- **🏷️ Card Tagging System**:
  - Categorize cards with custom tags (e.g. `java, oop, exam`), displayed as badges on flashcards and filterable in card management.
- **🔍 Instant Search & Multi-Level Filtering**:
  - Live real-time search across card questions, answers, and tags.
  - Filter by Card Type (Flashcard, Formula, Concept) and Leitner Box (Boxes 1–5).
- **✏️ In-Place Card Editor**:
  - Edit existing cards without losing their review history, statistics, or Leitner box levels (double-click row or click "Edit Card").
- **📂 CSV Import & Export**:
  - Export decks to standard CSV spreadsheets for backups or sharing.
  - Import flashcards directly from CSV/TSV files with automated validation.
- **📊 Analytics & Visual Progress**:
  - Custom stacked Java 2D bar charts tracking 7-day review history, Leitner box distribution, and upcoming review forecasts.
  - Automatic streak calculation and retention accuracy logging.
- **🧪 Built-in Test Suite & CI**:
  - Standalone verification suite with 25 automated unit tests (`TestRunner.java`).
  - Automated GitHub Actions CI workflow running across JDK 17 & 21.

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action | Where |
| :--- | :--- | :--- |
| `Space` | Flip card / Show Answer | Review Screen |
| `1` or `←` | Grade: **Again** (Reset) | When Answer is Revealed |
| `2` or `↓` | Grade: **Hard** (Conservative) | When Answer is Revealed |
| `3` or `→` | Grade: **Good** (Standard +1) | When Answer is Revealed |
| `4` or `↑` | Grade: **Easy** (Accelerated +2) | When Answer is Revealed |
| `Ctrl + 1` | Switch to **Review** Tab | Everywhere |
| `Ctrl + 2` | Switch to **Add / Manage** Tab | Everywhere |
| `Ctrl + 3` | Switch to **Progress** Tab | Everywhere |

---

## 📁 Project Structure

```text
Recall-Rhythm/
├── .github/workflows/
│   └── build.yml                       # GitHub Actions CI workflow
├── data/                               # Saved decks and revision logs
│   ├── java_oop_basics.json            # Sample OOP deck
│   ├── maths___physics_formulas.json   # Sample Formulas deck
│   └── review_history.csv              # Historical review records
├── AppClock.java                       # Real-time application clock & demo time travel
├── BarChartPanel.java                  # Custom Java 2D stacked bar chart component
├── Concept.java                        # Concept study item model
├── CorruptFileException.java           # Custom exception for corrupted deck files
├── Deck.java                           # Deck data model containing study items
├── DeckStorage.java                    # File IO, JSON persistence & CSV import/export
├── EmptyDeckException.java             # Exception thrown for empty study sessions
├── Flashcard.java                      # Standard flashcard model
├── FlipCardPanel.java                  # Interactive 3D flip card component with code highlighting
├── Formula.java                        # Formula study item model
├── InvalidCardException.java           # Validation exception for card data
├── ItemFactory.java                    # Factory for creating and restoring card types
├── JsonParser.java                     # Custom lightweight JSON parser
├── JsonSerializable.java               # Interface for JSON serializable objects
├── JsonUtil.java                       # JSON formatting and helper utilities
├── Main.java                           # Application entry point
├── MainPanel.java                      # Primary navigation and view container
├── ManagePanel.java                    # Card manager with search, filter, edit & CSV tools
├── ProgressPanel.java                  # Analytics, charts, and progress overview
├── Rating.java                         # 4-level rating enum (Again, Hard, Good, Easy)
├── RecallApp.java                      # Core application controller and state manager
├── Reviewable.java                     # Reviewable study item interface
├── ReviewLog.java                      # Review logging and history tracker
├── ReviewPanel.java                    # Active spaced repetition review session view
├── RoundButton.java                    # Custom rounded Swing button component
├── SampleData.java                     # Default decks and starter content generator
├── Scheduler.java                      # Spaced repetition scheduling algorithm
├── SoundEffect.java                    # Procedural audio generator using Java MIDI
├── StatChip.java                       # Modern badge/chip UI component
├── StudyItem.java                      # Abstract base class for study cards
├── TestRunner.java                     # Comprehensive test runner with 25 unit assertions
├── Theme.java                          # Color palette, Dark Mode tokens & style manager
├── pom.xml                             # Maven build and dependency configuration
└── .gitignore                          # Git ignore rules for Java builds
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 11, 17, or 21+ installed on your machine.

Verify your Java installation:
```bash
java -version
javac -version
```

---

### 🔨 Compilation & Running

#### Option 1: Standard Java (No tools required)

1. **Compile all source files:**
   ```bash
   javac *.java
   ```

2. **Run RecallRhythm:**
   ```bash
   java Main
   ```

3. **Run the Automated Test Suite:**
   ```bash
   java TestRunner
   ```

---

#### Option 2: Using Maven

1. **Build and package into an executable JAR:**
   ```bash
   mvn clean package
   ```

2. **Run via Maven:**
   ```bash
   mvn exec:java
   ```

---

## 🛠️ Tech Stack

- **Language**: Java (SE 11+)
- **GUI Framework**: Java Swing & AWT (Zero third-party GUI dependencies)
- **Audio**: Java MIDI Synthesizer API (`javax.sound.midi`)
- **Persistence**: Custom JSON file parser & CSV logger
- **Build System**: Maven (`pom.xml`) & GitHub Actions CI
- **Architecture**: Model-View-Controller (MVC) pattern with event-driven updates

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
