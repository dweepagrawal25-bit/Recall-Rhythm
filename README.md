# RecallRhythm 🎵🧠

**RecallRhythm** is a modern, intuitive Spaced Revision & Flashcard Planner desktop application built in Java Swing. It optimizes long-term memory retention using spaced repetition scheduling algorithms to help students and developers master concepts, formulas, and flashcards efficiently.

---

## ✨ Features

- **Spaced Repetition Scheduler**: Automatically calculates revision intervals (1 day, 3 days, 7 days, etc.) based on your performance and mastery scores.
- **Multiple Study Item Types**:
  - 📖 **Concepts**: Definitions, key takeaways, and detailed conceptual summaries.
  - 🧮 **Formulas**: Equations, variable breakdowns, and usage contexts.
  - ⚡ **Flashcards**: Quick question-and-answer recall tests.
- **Interactive UI**:
  - Dynamic 3D flip card animations with custom rendering.
  - Smooth rounded buttons and modern theme styling.
  - Real-time live clock display (`AppClock`).
- **Progress Tracking & Analytics**:
  - Interactive bar charts tracking review counts and mastery retention.
  - Historical study logs stored in CSV format (`review_history.csv`).
- **Deck Management & Serialization**:
  - Create, edit, and organize multiple custom decks.
  - JSON-based storage parser (`JsonParser` & `DeckStorage`) for easy import/export and human-readable backups.
- **Robust Error Handling**:
  - Graceful fallback for corrupted deck files (`CorruptFileException`) and empty deck edge cases.

---

## 📁 Project Structure

```text
Recall-Rhythm/
├── data/                               # Saved decks and revision logs
│   ├── java_oop_basics.json            # Sample OOP deck
│   ├── maths___physics_formulas.json   # Sample Formulas deck
│   └── review_history.csv              # Historical review records
├── AppClock.java                       # Real-time application clock
├── BarChartPanel.java                  # Custom bar chart component for review stats
├── Concept.java                        # Concept study item model
├── CorruptFileException.java           # Custom exception for corrupted deck files
├── Deck.java                           # Deck data model containing study items
├── DeckStorage.java                    # File IO and deck serialization logic
├── EmptyDeckException.java             # Exception thrown for empty study sessions
├── Flashcard.java                      # Standard flashcard model
├── FlipCardPanel.java                  # Interactive flip card UI component
├── Formula.java                        # Formula item model
├── InvalidCardException.java           # Validation exception for card data
├── ItemFactory.java                    # Factory for creating different card types
├── JsonParser.java                     # Custom lightweight JSON parser
├── JsonSerializable.java               # Interface for JSON serializable objects
├── JsonUtil.java                       # JSON formatting and helper utilities
├── Main.java                           # Application entry point
├── MainPanel.java                      # Primary navigation and view container
├── ManagePanel.java                    # Deck and card creation/management view
├── ProgressPanel.java                  # Analytics, charts, and progress overview
├── RecallApp.java                      # Core application controller and state manager
├── Reviewable.java                     # Reviewable study item interface
├── ReviewLog.java                      # Review logging and history tracker
├── ReviewPanel.java                    # Active spaced repetition review session view
├── RoundButton.java                    # Custom rounded Swing button component
├── SampleData.java                     # Default decks and starter content generator
├── Scheduler.java                      # Spaced repetition scheduling algorithm
├── StatChip.java                       # Modern badge/chip UI component
├── StudyItem.java                      # Abstract base class for study cards
├── Theme.java                          # Color palette and UI styling tokens
└── .gitignore                          # Git ignore rules for Java builds
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher installed on your machine.

Verify your Java installation:
```bash
java -version
javac -version
```

### 🔨 Compilation

Compile all Java source files from the project root directory:

```bash
javac *.java
```

### ▶️ Running the Application

Launch RecallRhythm:

```bash
java Main
```

---

## 🛠️ Tech Stack

- **Language**: Java (SE 11+)
- **GUI Framework**: Java Swing & AWT
- **Persistence**: Custom JSON file parser & CSV logger
- **Architecture**: Model-View-Controller (MVC) pattern with event-driven updates

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
