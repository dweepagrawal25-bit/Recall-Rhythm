import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Starter decks created on the very first run so the app is never empty. */
public final class SampleData {
    private SampleData() { }

    public static List<Deck> createDecks(LocalDate today) throws InvalidCardException {
        List<Deck> decks = new ArrayList<>();

        Deck oop = new Deck("Java OOP Basics");
        add(oop, "Concept", "What is Encapsulation?",
                "Bundling data and the methods that use it into one class, and hiding the fields behind private access.",
                "A BankAccount keeps its balance private and only allows changes through deposit() and withdraw().", today);
        add(oop, "Concept", "What is Polymorphism?",
                "One method call behaves differently depending on the real type of the object.",
                "shape.area() runs Circle's formula for a Circle and Rectangle's formula for a Rectangle.", today);
        add(oop, "Concept", "What is Abstraction?",
                "Showing only the essential features and hiding the implementation details, using abstract classes and interfaces.",
                "You drive a car using the steering wheel and pedals without knowing how the engine works.", today);
        add(oop, "Concept", "Abstract class vs interface?",
                "An interface is a pure contract (what to do). An abstract class can also hold fields and ready-made code.",
                "Payable is an interface; Participant is an abstract class with a name and an id.", today);
        add(oop, "Flashcard", "Name the four pillars of OOP.",
                "Encapsulation, Inheritance, Polymorphism and Abstraction.", "Think: A PIE", today);
        add(oop, "Flashcard", "Which keyword makes a class inherit from another class?",
                "extends", "", today);
        add(oop, "Flashcard", "Which keyword makes a class follow an interface?",
                "implements", "", today);
        add(oop, "Flashcard", "Can an abstract class be instantiated with new?",
                "No. Only its concrete subclasses can be instantiated.", "", today);
        add(oop, "Flashcard", "What does the final keyword do on a class?",
                "It prevents the class from being extended.", "", today);
        add(oop, "Flashcard", "Overloading vs overriding?",
                "Overloading: same method name, different parameters, same class. Overriding: a subclass replaces a parent method.",
                "Overloading is decided at compile time, overriding at run time.", today);
        decks.add(oop);

        Deck maths = new Deck("Maths & Physics Formulas");
        add(maths, "Formula", "Area of a circle", "A = \u03c0r\u00b2", "the radius is known", today);
        add(maths, "Formula", "Newton's second law", "F = m \u00d7 a", "force, mass or acceleration is missing", today);
        add(maths, "Formula", "Ohm's law", "V = I \u00d7 R", "finding voltage, current or resistance in a circuit", today);
        add(maths, "Formula", "Quadratic formula", "x = (\u2212b \u00b1 \u221a(b\u00b2 \u2212 4ac)) / 2a", "solving ax\u00b2 + bx + c = 0", today);
        add(maths, "Formula", "Kinetic energy", "KE = \u00bd m v\u00b2", "an object of mass m moves at speed v", today);
        add(maths, "Formula", "Compound interest", "A = P(1 + r/n)^(nt)", "money grows with interest added n times a year", today);
        decks.add(maths);

        return decks;
    }

    private static void add(Deck d, String type, String front, String back, String extra, LocalDate today)
            throws InvalidCardException {
        d.addItem(ItemFactory.create(type, d.nextId(), front, back, extra, today));
    }
}
