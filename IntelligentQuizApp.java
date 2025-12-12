import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Intelligent Quiz App - Enhanced with Swing UI
 * Features: File Upload, PDF Export, Interactive UI
 *
 * How to run:
 * 1) Compile: javac IntelligentQuizApp.java
 * 2) Run: java IntelligentQuizApp
 * 
 * Note: For PDF export, you'll need to add iText library or use built-in PDF generation
 */
public class IntelligentQuizApp extends JFrame {

    // ====== User Management ======
    private static final String USERS_FILE = "users.dat";
    private Map<String, String> users = new HashMap<>();

    private void loadUserData() {
        try {
            File f = new File(USERS_FILE);
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                users = (Map<String, String>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            users = new HashMap<>();
        }
    }

    // ====== Inner Model Classes ======
    static class Question {
        // type: 0 = MCQ, 1 = TRUE/FALSE, 2 = FILL_IN_BLANK
        int type;
        String text;
        String[] options;
        int correctIndex;
        String correctText;
        String userAnswer;
        boolean isCorrect;

        Question(int type, String text, String[] options, int correctIndex, String correctText) {
            this.type = type;
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
            this.correctText = correctText;
            this.userAnswer = "";
            this.isCorrect = false;
        }
    }

    static class QuizResult {
        String username;
        String category;
        String difficulty;
        int score;
        int total;
        double accuracy;
        String timestamp;
        List<Question> questions;

        QuizResult(String username, String category, String difficulty, int score, int total, 
                   double accuracy, String timestamp, List<Question> questions) {
            this.username = username;
            this.category = category;
            this.difficulty = difficulty;
            this.score = score;
            this.total = total;
            this.accuracy = accuracy;
            this.timestamp = timestamp;
            this.questions = questions;
        }
    }

    // ====== UI Components ======
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login Panel
    private JPanel loginPanel;
    private JTextField usernameField;
    private String currentUser = "Guest";

    // Dashboard Panel
    private JPanel dashboardPanel;
    private JLabel welcomeLabel;

    // Quiz Setup Panel
    private JPanel quizSetupPanel;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> difficultyCombo;
    private JSpinner timeLimitSpinner;
    private JSpinner numQuestionsSpinner;
    private JTextArea contentArea;
    private JLabel statusLabel;
    private JButton uploadButton;
    private String uploadedFileName = "";

    // Quiz Panel
    private JPanel quizPanel;
    private JLabel timerLabel;
    private JLabel questionCounterLabel;
    private JTextArea questionTextArea;
    private JPanel optionsPanel;
    private JLabel feedbackLabel;
    private JButton nextButton;
    private JProgressBar progressBar;
    private ButtonGroup optionGroup;
    private JTextField fillAnswerField;

    // Summary Panel
    private JPanel summaryPanel;
    private JTextArea summaryTextArea;
    private JButton downloadPdfButton;

    // Results Panel
    private JPanel resultsPanel;
    private JTextArea resultsTextArea;

    // Signup Panel
    private JPanel signupPanel;
    private JTextField signupUserField;
    private JPasswordField signupPassField;
    private JPasswordField signupConfirmPassField;
    private JPasswordField passwordField;

    // Manual Quiz Creator Panel
    private JPanel manualQuizPanel;
    private JTextArea manualQuestionArea;
    private JTextField manualOption1, manualOption2, manualOption3, manualOption4;
    private JComboBox<String> correctAnswerCombo;
    private JList<String> manualQuestionsList;
    private DefaultListModel<String> manualQuestionsListModel;
    private List<Question> manualQuestions = new ArrayList<>();

    // ====== Quiz Runtime Variables ======
    private List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int totalQuestions = 0;
    private int remainingSeconds = 0;
    private javax.swing.Timer quizTimer;
    private boolean quizActive = false;
    private String currentCategory = "";
    private String currentDifficulty = "";
    private QuizResult lastQuizResult = null;

    // ====== Sample Content ======
    private Map<String, String> sampleContent = new HashMap<>();
    private Random random = new Random();

    // ====== Constants ======
    private static final String RESULTS_FILE = "quiz_results.csv";
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);

    // ====== Constructor ======
    public IntelligentQuizApp() {
        loadUserData();
        initSampleContent();
        setupFrame();
        createPanels();
        setVisible(true);
    }

    // ====== Initialization ======
    private void initSampleContent() {
        sampleContent.put("Java",
                "Java is an object oriented programming language. " +
                "It runs on the Java Virtual Machine (JVM). " +
                "Classes and objects are core concepts in Java. " +
                "Inheritance and polymorphism are supported features. " +
                "The Java Standard Library provides collections and IO utilities. " +
                "Java uses garbage collection for memory management. " +
                "Exception handling is done with try-catch blocks.");

        sampleContent.put("AI",
                "Artificial Intelligence is the simulation of human intelligence in machines. " +
                "Machine learning is a subset of AI that learns from data. " +
                "Neural networks are used for deep learning applications. " +
                "AI can be applied in computer vision and language tasks. " +
                "Training data quality significantly affects model performance. " +
                "Natural language processing helps machines understand text. " +
                "Supervised learning uses labeled training data.");

        sampleContent.put("Networking",
                "Computer networking connects multiple computers together. " +
                "The TCP protocol provides reliable communication channels. " +
                "IP addressing uniquely identifies hosts on networks. " +
                "Routers forward packets between different networks. " +
                "The OSI model has seven distinct layers. " +
                "DNS translates domain names to IP addresses. " +
                "HTTP is the protocol used for web communication.");
    }

    private void setupFrame() {
        setTitle("Intelligent Quiz Application");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);
    }

    private void createPanels() {
        createLoginPanel();
        createDashboardPanel();
        createQuizSetupPanel();
        createManualQuizPanel();
        createQuizPanel();
        createSummaryPanel();
        createResultsPanel();
        createSignupPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(quizSetupPanel, "setup");
        mainPanel.add(manualQuizPanel, "manual");
        mainPanel.add(quizPanel, "quiz");
        mainPanel.add(summaryPanel, "summary");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(signupPanel, "signup");

        cardLayout.show(mainPanel, "login");
    }

    // ====== Login Panel ======
    private void createLoginPanel() {
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 100));
        JLabel titleLabel = new JLabel("Intelligent Quiz Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        loginPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel loginBox = new JPanel(new GridLayout(0, 1, 10, 10));
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2, true),
            new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel welcomeLabel = new JLabel("Welcome! Please login to continue", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginBox.add(welcomeLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginBox.add(userLabel);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        loginBox.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginBox.add(passLabel);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        loginBox.add(passwordField);

        JButton loginButton = createStyledButton("Login", PRIMARY_COLOR);
        loginButton.addActionListener(e -> handleLogin());
        loginBox.add(loginButton);

        JButton signupButton = createStyledButton("Create Account", ACCENT_COLOR);
        signupButton.addActionListener(e -> cardLayout.show(mainPanel, "signup"));
        loginBox.add(signupButton);

        centerPanel.add(loginBox);
        loginPanel.add(centerPanel, BorderLayout.CENTER);
    }

    // ====== Signup Panel ======
    private void createSignupPanel() {
        signupPanel = new JPanel(new BorderLayout());
        signupPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 100));
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        signupPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel signupBox = new JPanel(new GridLayout(0, 1, 10, 10));
        signupBox.setBackground(Color.WHITE);
        signupBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2, true),
            new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupBox.add(userLabel);

        signupUserField = new JTextField(20);
        signupUserField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupUserField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        signupBox.add(signupUserField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupBox.add(passLabel);

        signupPassField = new JPasswordField(20);
        signupPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupPassField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        signupBox.add(signupPassField);

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupBox.add(confirmPassLabel);

        signupConfirmPassField = new JPasswordField(20);
        signupConfirmPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupConfirmPassField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        signupBox.add(signupConfirmPassField);

        JButton registerButton = createStyledButton("Register", SUCCESS_COLOR);
        registerButton.addActionListener(e -> handleSignup());
        signupBox.add(registerButton);

        JButton backButton = createStyledButton("Back to Login", Color.GRAY);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        signupBox.add(backButton);

        centerPanel.add(signupBox);
        signupPanel.add(centerPanel, BorderLayout.CENTER);
    }

    // ====== Dashboard Panel ======
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = createStyledButton("Log Out", ERROR_COLOR);
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.setMaximumSize(new Dimension(120, 40));
        logoutButton.addActionListener(e -> handleLogout());

        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(logoutButton);

        dashboardPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with cards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // Create Quiz Card
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        JPanel createQuizCard = createDashboardCard(
            "Create & Start Quiz",
            "Upload content or use samples to generate intelligent questions",
            "🎯",
            SUCCESS_COLOR,
            e -> cardLayout.show(mainPanel, "setup")
        );
        centerPanel.add(createQuizCard, gbc);

        // Manual Quiz Creator Card
        gbc.gridy = 1;
        JPanel manualCard = createDashboardCard(
            "Manual Quiz Creator",
            "Create your own quiz by adding questions with 4 options",
            "✏️",
            new Color(156, 39, 176), // Purple
            e -> cardLayout.show(mainPanel, "manual")
        );
        centerPanel.add(manualCard, gbc);

        // Quick Demo Card
        gbc.gridy = 2;
        JPanel demoCard = createDashboardCard(
            "Quick Demo Quiz",
            "Instantly start a demo quiz with Java questions",
            "🚀",
            ACCENT_COLOR,
            e -> {
                String demoCategory = "Java";
                String demoContent = sampleContent.get(demoCategory);
                startQuiz(demoContent, 5, demoCategory, "Medium", 5);
            }
        );
        centerPanel.add(demoCard, gbc);

        // View Results Card
        gbc.gridy = 3;
        JPanel resultsCard = createDashboardCard(
            "View Results Dashboard",
            "Check your quiz history and download reports",
            "📊",
            PRIMARY_COLOR,
            e -> {
                loadResults();
                cardLayout.show(mainPanel, "results");
            }
        );
        centerPanel.add(resultsCard, gbc);

        // Exit Card
        gbc.gridy = 4;
        JPanel exitCard = createDashboardCard(
            "Exit Application",
            "Close the application",
            "🚪",
            ERROR_COLOR,
            e -> System.exit(0)
        );
        centerPanel.add(exitCard, gbc);

        dashboardPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createDashboardCard(String title, String description, String emoji, 
                                       Color color, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color, 2, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(600, 100));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        leftPanel.add(emojiLabel, BorderLayout.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        textPanel.add(titleLabel);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        textPanel.add(descLabel);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    // ====== Quiz Setup Panel ======
    private void createQuizSetupPanel() {
        quizSetupPanel = new JPanel(new BorderLayout());
        quizSetupPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        JLabel headerLabel = new JLabel("Create Quiz from Learning Material");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        quizSetupPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Category
        formPanel.add(createFormLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"Java", "AI", "Networking", "Custom"});
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(categoryCombo);

        // Difficulty
        formPanel.add(createFormLabel("Difficulty:"));
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(difficultyCombo);

        // Time limit
        formPanel.add(createFormLabel("Time Limit (minutes):"));
        timeLimitSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 60, 1));
        timeLimitSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(timeLimitSpinner);

        // Number of questions
        formPanel.add(createFormLabel("Number of Questions:"));
        numQuestionsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        numQuestionsSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(numQuestionsSpinner);

        // Upload button
        formPanel.add(createFormLabel("Upload Text File:"));
        uploadButton = createStyledButton("📁 Browse & Upload TXT File", ACCENT_COLOR);
        uploadButton.addActionListener(e -> uploadTextFile());
        formPanel.add(uploadButton);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(BACKGROUND_COLOR);

        JLabel contentLabel = new JLabel("Learning Content (paste text or upload file):");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(contentLabel, BorderLayout.NORTH);

        contentArea = new JTextArea(10, 80);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(new LineBorder(Color.GRAY, 1));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(contentPanel, BorderLayout.CENTER);

        // Status label
        statusLabel = new JLabel("Ready to create quiz. Upload a file or paste content.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        quizSetupPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        JButton backButton = createStyledButton("← Back", Color.GRAY);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        bottomPanel.add(backButton);

        JButton addManualBtn = createStyledButton("+ Add Manual Questions", new Color(156, 39, 176));
        addManualBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to add your own questions to this quiz?\n\n" +
                "Click YES to go to manual question creator.\n" +
                "The auto-generated questions will be created when you start the quiz.",
                "Add Manual Questions", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                cardLayout.show(mainPanel, "manual");
            }
        });
        bottomPanel.add(addManualBtn);

        JButton startButton = createStyledButton("Generate & Start Quiz →", SUCCESS_COLOR);
        startButton.addActionListener(e -> startQuizFromSetup());
        bottomPanel.add(startButton);

        quizSetupPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // ====== Manual Quiz Creator Panel ======
    private void createManualQuizPanel() {
        manualQuizPanel = new JPanel(new BorderLayout());
        manualQuizPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        JLabel headerLabel = new JLabel("Manual Quiz Creator - Add Your Own Questions");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        manualQuizPanel.add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BACKGROUND_COLOR);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left panel - Question Form
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel inputPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        JLabel questionLabel = new JLabel("Question:");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(questionLabel);

        manualQuestionArea = new JTextArea(3, 40);
        manualQuestionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualQuestionArea.setLineWrap(true);
        manualQuestionArea.setWrapStyleWord(true);
        manualQuestionArea.setBorder(new LineBorder(Color.GRAY, 1));
        JScrollPane questionScroll = new JScrollPane(manualQuestionArea);
        inputPanel.add(questionScroll);

        JLabel optionsLabel = new JLabel("Options:");
        optionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(optionsLabel);

        manualOption1 = new JTextField();
        manualOption1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualOption1.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        inputPanel.add(createLabeledField("A)", manualOption1));

        manualOption2 = new JTextField();
        manualOption2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualOption2.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        inputPanel.add(createLabeledField("B)", manualOption2));

        manualOption3 = new JTextField();
        manualOption3.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualOption3.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        inputPanel.add(createLabeledField("C)", manualOption3));

        manualOption4 = new JTextField();
        manualOption4.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualOption4.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        inputPanel.add(createLabeledField("D)", manualOption4));

        JLabel correctLabel = new JLabel("Correct Answer:");
        correctLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(correctLabel);

        correctAnswerCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        correctAnswerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputPanel.add(correctAnswerCombo);

        JButton addQuestionBtn = createStyledButton("+ Add Question to Quiz", SUCCESS_COLOR);
        addQuestionBtn.addActionListener(e -> addManualQuestion());
        inputPanel.add(addQuestionBtn);

        formPanel.add(inputPanel, BorderLayout.CENTER);

        // Right panel - Questions List
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_COLOR, 2),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel listLabel = new JLabel("Added Questions (" + manualQuestions.size() + "):");
        listLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        listPanel.add(listLabel, BorderLayout.NORTH);

        manualQuestionsListModel = new DefaultListModel<>();
        manualQuestionsList = new JList<>(manualQuestionsListModel);
        manualQuestionsList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        manualQuestionsList.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane listScroll = new JScrollPane(manualQuestionsList);
        listPanel.add(listScroll, BorderLayout.CENTER);

        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        listButtons.setBackground(Color.WHITE);
        
        JButton removeBtn = createStyledButton("Remove Selected", ERROR_COLOR);
        removeBtn.addActionListener(e -> removeManualQuestion());
        listButtons.add(removeBtn);

        JButton clearBtn = createStyledButton("Clear All", Color.GRAY);
        clearBtn.addActionListener(e -> clearManualQuestions());
        listButtons.add(clearBtn);

        listPanel.add(listButtons, BorderLayout.SOUTH);

        // Add panels to split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, listPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);
        mainContent.add(splitPane, BorderLayout.CENTER);

        manualQuizPanel.add(mainContent, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        JButton backButton = createStyledButton("← Back to Dashboard", Color.GRAY);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        bottomPanel.add(backButton);

        JButton autoGenBtn = createStyledButton("Go to Auto-Generate", ACCENT_COLOR);
        autoGenBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Your manual questions are saved!\n\n" +
                "Go to 'Create & Start Quiz' to add auto-generated questions.\n" +
                "Both manual and auto-generated questions will be combined.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "setup");
        });
        bottomPanel.add(autoGenBtn);

        JButton startQuizBtn = createStyledButton("Start Quiz (Manual Only) →", SUCCESS_COLOR);
        startQuizBtn.addActionListener(e -> startManualQuiz());
        bottomPanel.add(startQuizBtn);

        manualQuizPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setPreferredSize(new Dimension(30, 30));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    // ====== Quiz Panel ======
    private void createQuizPanel() {
        quizPanel = new JPanel(new BorderLayout());
        quizPanel.setBackground(BACKGROUND_COLOR);

        // Top panel with timer and counter
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_COLOR);
        topPanel.setPreferredSize(new Dimension(1000, 60));
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        timerLabel = new JLabel("⏱ Time: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(Color.WHITE);
        topPanel.add(timerLabel, BorderLayout.WEST);

        questionCounterLabel = new JLabel("Question 0 / 0");
        questionCounterLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        questionCounterLabel.setForeground(Color.WHITE);
        topPanel.add(questionCounterLabel, BorderLayout.EAST);

        quizPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Question text
        JPanel questionPanel = new JPanel(new BorderLayout(5, 5));
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel questionLabel = new JLabel("Question:");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        questionTextArea = new JTextArea(4, 80);
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setBackground(Color.WHITE);
        questionPanel.add(questionTextArea, BorderLayout.CENTER);

        centerPanel.add(questionPanel, BorderLayout.NORTH);

        // Options panel
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(optionsPanel, BorderLayout.CENTER);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBar.setPreferredSize(new Dimension(900, 30));
        progressBar.setForeground(SUCCESS_COLOR);
        centerPanel.add(progressBar, BorderLayout.SOUTH);

        quizPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        feedbackLabel = new JLabel("Select an answer and click Next");
        feedbackLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        feedbackLabel.setForeground(Color.GRAY);
        bottomPanel.add(feedbackLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton cancelButton = createStyledButton("Cancel Quiz", ERROR_COLOR);
        cancelButton.addActionListener(e -> cancelQuiz());
        buttonPanel.add(cancelButton);

        nextButton = createStyledButton("Next Question →", SUCCESS_COLOR);
        nextButton.addActionListener(e -> handleNextQuestion());
        buttonPanel.add(nextButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        quizPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // ====== Summary Panel ======
    private void createSummaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        JLabel headerLabel = new JLabel("Quiz Summary");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        summaryPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        summaryTextArea = new JTextArea();
        summaryTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        summaryTextArea.setEditable(false);
        summaryTextArea.setBackground(Color.WHITE);
        summaryTextArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(summaryTextArea);
        scrollPane.setBorder(new LineBorder(PRIMARY_COLOR, 2));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        summaryPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        downloadPdfButton = createStyledButton("📥 Download PDF Report", ACCENT_COLOR);
        downloadPdfButton.addActionListener(e -> downloadPdfReport());
        bottomPanel.add(downloadPdfButton);

        JButton viewResultsButton = createStyledButton("📊 View All Results", PRIMARY_COLOR);
        viewResultsButton.addActionListener(e -> {
            loadResults();
            cardLayout.show(mainPanel, "results");
        });
        bottomPanel.add(viewResultsButton);

        JButton backButton = createStyledButton("← Back to Dashboard", Color.GRAY);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        bottomPanel.add(backButton);

        summaryPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // ====== Results Panel ======
    private void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        JLabel headerLabel = new JLabel("Results Dashboard");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        resultsPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        resultsTextArea = new JTextArea();
        resultsTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultsTextArea.setEditable(false);
        resultsTextArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resultsTextArea);
        scrollPane.setBorder(new LineBorder(PRIMARY_COLOR, 2));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        resultsPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        JButton refreshButton = createStyledButton("🔄 Refresh", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> loadResults());
        bottomPanel.add(refreshButton);

        JButton backButton = createStyledButton("← Back to Dashboard", Color.GRAY);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        bottomPanel.add(backButton);

        resultsPanel.add(bottomPanel, BorderLayout.SOUTH);
    }



    // ====== Helper Methods ======
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    // ====== Action Handlers ======
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (users.containsKey(username) && users.get(username).equals(password)) {
            currentUser = username;
            welcomeLabel.setText("Welcome, " + currentUser + "! 👋");
            cardLayout.show(mainPanel, "dashboard");
            // Clear fields
            usernameField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignup() {
        String username = signupUserField.getText().trim();
        String password = new String(signupPassField.getPassword());
        String confirmPassword = new String(signupConfirmPassField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Incomplete Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if username already exists
        if (users.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another one.", "Signup Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add new user
        users.put(username, password);
        saveUserData();

        JOptionPane.showMessageDialog(this, "Signup successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "login");
        
        // Clear fields
        signupUserField.setText("");
        signupPassField.setText("");
        signupConfirmPassField.setText("");
    }

    private void saveUserData() {
        try {
            FileOutputStream fos = new FileOutputStream(USERS_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addManualQuestion() {
        String questionText = manualQuestionArea.getText().trim();
        String opt1 = manualOption1.getText().trim();
        String opt2 = manualOption2.getText().trim();
        String opt3 = manualOption3.getText().trim();
        String opt4 = manualOption4.getText().trim();
        
        if (questionText.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || opt4.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Incomplete Question", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] options = {opt1, opt2, opt3, opt4};
        int correctIndex = correctAnswerCombo.getSelectedIndex();
        
        Question q = new Question(0, questionText, options, correctIndex, null);
        manualQuestions.add(q);
        
        // Update list display
        manualQuestionsListModel.addElement((manualQuestions.size()) + ". " + questionText.substring(0, Math.min(60, questionText.length())) + "...");
        
        // Clear fields
        manualQuestionArea.setText("");
        manualOption1.setText("");
        manualOption2.setText("");
        manualOption3.setText("");
        manualOption4.setText("");
        correctAnswerCombo.setSelectedIndex(0);
        
        JOptionPane.showMessageDialog(this, "Question added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void removeManualQuestion() {
        int selectedIndex = manualQuestionsList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < manualQuestions.size()) {
            manualQuestions.remove(selectedIndex);
            manualQuestionsListModel.remove(selectedIndex);
            
            // Update numbering
            manualQuestionsListModel.clear();
            for (int i = 0; i < manualQuestions.size(); i++) {
                Question q = manualQuestions.get(i);
                manualQuestionsListModel.addElement((i + 1) + ". " + q.text.substring(0, Math.min(60, q.text.length())) + "...");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a question to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void clearManualQuestions() {
        if (manualQuestions.isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all questions?", "Clear All", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            manualQuestions.clear();
            manualQuestionsListModel.clear();
        }
    }
    
    private void startManualQuiz() {
        if (manualQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one question before starting the quiz.", 
                "No Questions", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        currentQuestions = new ArrayList<>(manualQuestions);
        currentCategory = "Manual Quiz";
        currentDifficulty = "Custom";
        totalQuestions = currentQuestions.size();
        currentQuestionIndex = 0;
        correctCount = 0;
        remainingSeconds = Math.max(2, currentQuestions.size()) * 60; // 1 min per question, minimum 2 min
        
        startTimer();
        quizActive = true;
        
        showCurrentQuestion();
        cardLayout.show(mainPanel, "quiz");
    }

    private void uploadTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Text File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            uploadedFileName = selectedFile.getName();

            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line).append(" ");
                }
                reader.close();

                String fileContent = content.toString().trim();
                if (fileContent.isEmpty()) {
                    statusLabel.setText("⚠ The selected file is empty. Please choose a file with content.");
                    statusLabel.setForeground(ERROR_COLOR);
                } else {
                    contentArea.setText(fileContent);
                    statusLabel.setText("✓ File uploaded successfully: " + uploadedFileName);
                    statusLabel.setForeground(SUCCESS_COLOR);
                    uploadButton.setText("✓ " + uploadedFileName);
                }
            } catch (IOException ex) {
                statusLabel.setText("⚠ Error reading file: " + ex.getMessage());
                statusLabel.setForeground(ERROR_COLOR);
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startQuizFromSetup() {
        String category = (String) categoryCombo.getSelectedItem();
        String difficulty = (String) difficultyCombo.getSelectedItem();
        int numQuestions = (Integer) numQuestionsSpinner.getValue();
        int minutes = (Integer) timeLimitSpinner.getValue();

        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            if (!category.equals("Custom")) {
                content = sampleContent.get(category);
                statusLabel.setText("Using built-in sample content for " + category);
                statusLabel.setForeground(Color.BLUE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please paste some content or upload a file for Custom category.",
                        "No Content", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        startQuiz(content, numQuestions, category, difficulty, minutes);
    }

    private void startQuiz(String content, int numQuestions, String category, String difficulty, int minutes) {
        try {
            // Generate questions from content
            List<Question> generatedQuestions = generateQuestions(content, numQuestions, category, difficulty);
            
            if (generatedQuestions == null) {
                generatedQuestions = new ArrayList<>();
            }
            
            currentQuestions = new ArrayList<>(generatedQuestions);
            
            // Merge manual questions with auto-generated questions
            if (!manualQuestions.isEmpty()) {
                int totalManual = manualQuestions.size();
                int totalAuto = currentQuestions.size();
                currentQuestions.addAll(new ArrayList<>(manualQuestions));
                
                // Shuffle to mix manual and auto-generated questions
                Collections.shuffle(currentQuestions, random);
                
                JOptionPane.showMessageDialog(this,
                    "Quiz created with:\n" +
                    "• " + totalAuto + " auto-generated questions\n" +
                    "• " + totalManual + " manual questions\n" +
                    "Total: " + currentQuestions.size() + " questions",
                    "Quiz Ready", JOptionPane.INFORMATION_MESSAGE);
            }
            
            if (currentQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Could not generate questions from the content. Try adding more sentences or add manual questions.",
                        "Generation Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentCategory = category;
            currentDifficulty = difficulty;
            totalQuestions = currentQuestions.size();
            currentQuestionIndex = 0;
            correctCount = 0;
            remainingSeconds = minutes * 60;

            startTimer();
            quizActive = true;

            showCurrentQuestion();
            cardLayout.show(mainPanel, "quiz");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error starting quiz: " + ex.getMessage() + "\n\nPlease try again or check your content.",
                "Quiz Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelQuiz() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel the quiz? Your progress will be lost.",
                "Cancel Quiz", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            stopTimer();
            quizActive = false;
            cardLayout.show(mainPanel, "dashboard");
        }
    }

    private void showCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= currentQuestions.size()) return;

        Question q = currentQuestions.get(currentQuestionIndex);
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + " / " + currentQuestions.size());
        questionTextArea.setText(q.text);

        optionsPanel.removeAll();
        // Reset layout to BorderLayout to ensure grid panel expands properly
        optionsPanel.setLayout(new BorderLayout());
        
        feedbackLabel.setText("Select your answer and click Next");
        feedbackLabel.setForeground(Color.GRAY);

        optionGroup = null;
        fillAnswerField = null;

        if (q.type == 0 || q.type == 1) {
            // MCQ or True/False - Display in 2x2 GRID like reference image
            optionGroup = new ButtonGroup();
            
            // Create a 2x2 grid panel for options (like the reference image)
            // Use GridLayout(0, 2) to ensure 2 columns, rows calculated automatically
            JPanel gridPanel = new JPanel(new GridLayout(0, 2, 20, 20)); 
            gridPanel.setBackground(BACKGROUND_COLOR);
            gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Define option labels (A), (B), (C), (D)
            char[] optionLabels = {'A', 'B', 'C', 'D'};
            
            for (int i = 0; i < q.options.length; i++) {
                // Create a styled button-like panel for each option
                JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                optionPanel.setBackground(new Color(45, 45, 65)); // Dark blue-purple background like reference
                optionPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(100, 150, 200), 2, true), // Rounded border
                    new EmptyBorder(15, 20, 15, 20)
                ));
                // Reduce preferred size slightly to ensure 2 columns fit
                optionPanel.setPreferredSize(new Dimension(300, 70));
                
                // Create label circle with letter (A, B, C, D)
                JLabel labelCircle = new JLabel(String.valueOf(optionLabels[i]));
                labelCircle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                labelCircle.setForeground(Color.BLACK);
                labelCircle.setBackground(new Color(100, 255, 150)); // Green circle background like reference
                labelCircle.setOpaque(true);
                labelCircle.setPreferredSize(new Dimension(40, 40));
                labelCircle.setHorizontalAlignment(SwingConstants.CENTER);
                labelCircle.setVerticalAlignment(SwingConstants.CENTER);
                labelCircle.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                
                // Create radio button with option text
                JRadioButton radioButton = new JRadioButton(q.options[i]);
                radioButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                radioButton.setForeground(Color.WHITE);
                radioButton.setBackground(new Color(45, 45, 65));
                radioButton.setFocusPainted(false);
                
                // Add components to option panel
                optionPanel.add(labelCircle);
                optionPanel.add(radioButton);
                
                // Add hover and selection effects
                final JPanel finalOptionPanel = optionPanel;
                final JLabel finalLabelCircle = labelCircle;
                
                radioButton.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        finalOptionPanel.setBackground(new Color(60, 60, 85));
                        finalOptionPanel.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(150, 200, 255), 3, true),
                            new EmptyBorder(14, 19, 14, 19)
                        ));
                    }
                    public void mouseExited(MouseEvent e) {
                        if (!radioButton.isSelected()) {
                            finalOptionPanel.setBackground(new Color(45, 45, 65));
                            finalOptionPanel.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(new Color(100, 150, 200), 2, true),
                                new EmptyBorder(15, 20, 15, 20)
                            ));
                        }
                    }
                });
                
                // Selection highlighting
                radioButton.addItemListener(e -> {
                    if (radioButton.isSelected()) {
                        finalOptionPanel.setBackground(new Color(60, 60, 100));
                        finalOptionPanel.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(100, 255, 150), 3, true), // Green border when selected
                            new EmptyBorder(14, 19, 14, 19)
                        ));
                        finalLabelCircle.setBackground(new Color(150, 255, 200)); // Brighter green
                    } else {
                        finalOptionPanel.setBackground(new Color(45, 45, 65));
                        finalOptionPanel.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(100, 150, 200), 2, true),
                            new EmptyBorder(15, 20, 15, 20)
                        ));
                        finalLabelCircle.setBackground(new Color(100, 255, 150));
                    }
                });
                
                // Make the entire panel clickable
                optionPanel.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        radioButton.setSelected(true);
                    }
                });
                
                optionGroup.add(radioButton);
                gridPanel.add(optionPanel);
            }
            
            optionsPanel.add(gridPanel, BorderLayout.CENTER);
        } else if (q.type == 2) {
            // Fill in the blank
            JPanel fillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            fillPanel.setBackground(BACKGROUND_COLOR);
            JLabel fillLabel = new JLabel("Your answer:");
            fillLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            fillPanel.add(fillLabel);

            fillAnswerField = new JTextField(30);
            fillAnswerField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fillAnswerField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
            ));
            fillPanel.add(fillAnswerField);

            optionsPanel.add(fillPanel, BorderLayout.NORTH);
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();

        int progress = ((currentQuestionIndex + 1) * 100) / currentQuestions.size();
        progressBar.setValue(progress);
        progressBar.setString("Progress: " + (currentQuestionIndex + 1) + " / " + currentQuestions.size());
    }

    private void handleNextQuestion() {
        if (!quizActive) {
            feedbackLabel.setText("Quiz not active.");
            return;
        }

        if (!evaluateCurrentAnswer()) {
            JOptionPane.showMessageDialog(this,
                    "Please select or enter an answer before continuing.",
                    "No Answer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= currentQuestions.size()) {
            endQuiz();
        } else {
            // Small delay before showing next question
            javax.swing.Timer delayTimer = new javax.swing.Timer(1500, e -> showCurrentQuestion());
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
    }

    private boolean evaluateCurrentAnswer() {
        Question q = currentQuestions.get(currentQuestionIndex);

        if (q.type == 0 || q.type == 1) {
            if (optionGroup == null) return false;

            ButtonModel selectedModel = optionGroup.getSelection();
            if (selectedModel == null) return false;

            Enumeration<AbstractButton> buttons = optionGroup.getElements();
            int selectedIndex = -1;
            int index = 0;
            while (buttons.hasMoreElements()) {
                AbstractButton button = buttons.nextElement();
                if (button.getModel() == selectedModel) {
                    selectedIndex = index;
                    q.userAnswer = button.getText();
                    break;
                }
                index++;
            }

            if (selectedIndex == -1) return false;

            if (selectedIndex == q.correctIndex) {
                correctCount++;
                q.isCorrect = true;
                feedbackLabel.setText("✓ Correct!");
                feedbackLabel.setForeground(SUCCESS_COLOR);
            } else {
                q.isCorrect = false;
                feedbackLabel.setText("✗ Incorrect. Correct answer: " + q.options[q.correctIndex]);
                feedbackLabel.setForeground(ERROR_COLOR);
            }
            return true;
        } else {
            if (fillAnswerField == null) return false;
            String answer = fillAnswerField.getText().trim();
            if (answer.isEmpty()) return false;

            q.userAnswer = answer;

            if (answer.equalsIgnoreCase(q.correctText)) {
                correctCount++;
                q.isCorrect = true;
                feedbackLabel.setText("✓ Correct!");
                feedbackLabel.setForeground(SUCCESS_COLOR);
            } else {
                q.isCorrect = false;
                feedbackLabel.setText("✗ Incorrect. Correct answer: " + q.correctText);
                feedbackLabel.setForeground(ERROR_COLOR);
            }
            return true;
        }
    }

    private void endQuiz() {
        stopTimer();
        quizActive = false;

        double accuracy = (correctCount * 100.0) / totalQuestions;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        lastQuizResult = new QuizResult(currentUser, currentCategory, currentDifficulty,
                correctCount, totalQuestions, accuracy, timestamp, new ArrayList<>(currentQuestions));

        displaySummary();
        appendResultToCSV(correctCount, totalQuestions, accuracy);

        cardLayout.show(mainPanel, "summary");
    }

    private void displaySummary() {
        if (lastQuizResult == null) return;

        StringBuilder summary = new StringBuilder();
        summary.append("════════════════════════════════════════════════════════\n");
        summary.append("                    QUIZ SUMMARY                      \n");
        summary.append("════════════════════════════════════════════════════════\n\n");

        summary.append("User: ").append(lastQuizResult.username).append("\n");
        summary.append("Category: ").append(lastQuizResult.category).append("\n");
        summary.append("Difficulty: ").append(lastQuizResult.difficulty).append("\n");
        summary.append("Date & Time: ").append(lastQuizResult.timestamp).append("\n\n");

        summary.append("────────────────────────────────────────────────────────\n");
        summary.append("Score: ").append(lastQuizResult.score).append(" / ").append(lastQuizResult.total).append("\n");
        summary.append("Accuracy: ").append(String.format("%.2f", lastQuizResult.accuracy)).append("%\n");
        summary.append("────────────────────────────────────────────────────────\n\n");

        summary.append("DETAILED RESULTS:\n\n");

        for (int i = 0; i < lastQuizResult.questions.size(); i++) {
            Question q = lastQuizResult.questions.get(i);
            summary.append("Question ").append(i + 1).append(": ");
            summary.append(q.isCorrect ? "✓ CORRECT" : "✗ INCORRECT").append("\n");
            summary.append("Q: ").append(q.text).append("\n");
            summary.append("Your Answer: ").append(q.userAnswer).append("\n");

            if (!q.isCorrect) {
                if (q.type == 2) {
                    summary.append("Correct Answer: ").append(q.correctText).append("\n");
                } else {
                    summary.append("Correct Answer: ").append(q.options[q.correctIndex]).append("\n");
                }
            }
            summary.append("\n");
        }

        summary.append("════════════════════════════════════════════════════════\n");

        summaryTextArea.setText(summary.toString());
        summaryTextArea.setCaretPosition(0);
    }

    // ====== Question Generation ======
    private List<Question> generateQuestions(String content, int maxQuestions,
                                            String category, String difficulty) {
        List<Question> questions = new ArrayList<>();
        
        try {
            if (content == null || content.trim().isEmpty()) {
                System.out.println("Error: Content is empty");
                return questions;
            }

            String[] sentences = content.split("[\\.\\?!]");
            List<String> cleaned = new ArrayList<>();
            for (String s : sentences) {
                String t = s.trim();
                if (t.split("\\s+").length >= 4) {
                    cleaned.add(t);
                }
            }

            if (cleaned.isEmpty()) {
                System.out.println("Error: No valid sentences found");
                return questions;
            }
            
            System.out.println("Generating questions from " + cleaned.size() + " sentences...");

            int idx = 0;
            // If the content came from an uploaded/custom file, prefer MCQs so users get
            // multiple-choice questions (not only True/False). We detect this by
            // checking the category or whether an uploaded file name is present.
            boolean forceMCQ = "Custom".equalsIgnoreCase(category) || (uploadedFileName != null && !uploadedFileName.isEmpty());

            while (questions.size() < maxQuestions && idx < cleaned.size() * 2) {
                String sentence = cleaned.get(idx % cleaned.size());
                int type;

                if (forceMCQ) {
                    type = 0; // force MCQ for uploaded/custom content
                } else {
                    // keep a mix for sample content: rotate types for variety
                    type = questions.size() % 3;
                }

                try {
                    if (type == 0) {
                        Question q = generateMCQ(sentence);
                        if (q != null) {
                            questions.add(q);
                            System.out.println("Generated MCQ " + questions.size());
                        }
                    } else if (type == 1) {
                        Question q = generateTrueFalse(sentence);
                        if (q != null) {
                            questions.add(q);
                            System.out.println("Generated True/False " + questions.size());
                        }
                    } else {
                        Question q = generateFillBlank(sentence);
                        if (q != null) {
                            questions.add(q);
                            System.out.println("Generated Fill Blank " + questions.size());
                        } else {
                            q = generateTrueFalse(sentence);
                            if (q != null) {
                                questions.add(q);
                                System.out.println("Generated True/False (fallback) " + questions.size());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error generating question from sentence: " + e.getMessage());
                    e.printStackTrace();
                }
                
                idx++;
            }
            
            System.out.println("Total questions generated: " + questions.size());
        } catch (Exception e) {
            System.out.println("Error in generateQuestions: " + e.getMessage());
            e.printStackTrace();
        }

        Collections.shuffle(questions, random);
        if (questions.size() > maxQuestions) {
            questions = questions.subList(0, maxQuestions);
        }
        return questions;
    }

    private Question generateMCQ(String sentence) {
        try {
            if (sentence == null || sentence.trim().isEmpty()) {
                return null;
            }
            
            // Extract key information from the sentence to create meaningful MCQ
            String[] words = sentence.trim().split("\\s+");
            
            if (words.length == 0) {
                return null;
            }
            
            // Try to identify a key term or concept in the sentence
            String keyTerm = null;
            String cleanKeyTerm = null;
            String questionText;
            
            // Look for important words (nouns, technical terms)
            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
                if (cleanWord.length() > 5 && !isCommonWord(cleanWord)) {
                    keyTerm = word;
                    cleanKeyTerm = cleanWord;
                    break;
                }
            }
            
            if (keyTerm != null && cleanKeyTerm != null && words.length > 5) {
                // Create a question by replacing the key term with a blank or asking about it
                questionText = "According to the content:\n\n\"" + sentence + "\"\n\nWhat word best fits in place of \"" + cleanKeyTerm + "\"?";
                
                // Generate plausible options including the correct answer
                String correct = cleanKeyTerm;
                List<String> distractors = generateDistractors(cleanKeyTerm, sentence);
                
                if (distractors == null || distractors.isEmpty()) {
                    // Fallback if distractor generation failed
                    distractors = new ArrayList<>();
                    distractors.add("Option A");
                    distractors.add("Option B");
                    distractors.add("Option C");
                }
                
                List<String> opts = new ArrayList<>();
                opts.add(correct);
                opts.addAll(distractors);
                Collections.shuffle(opts, random);
                int correctIndex = opts.indexOf(correct);
                
                String[] formattedOptions = new String[Math.min(4, opts.size())];
                for (int i = 0; i < formattedOptions.length; i++) {
                    formattedOptions[i] = opts.get(i);
                }
                
                return new Question(0, questionText, formattedOptions, correctIndex, null);
            } else {
                // Fallback: comprehension-style question
                questionText = "Which statement is correct about the following?\n\n" + sentence;
                
                String correct = "The statement is accurate as presented";
                String[] distractors = new String[]{
                    "The statement is completely false",
                    "The opposite meaning is true",
                    "This information is not mentioned"
                };
                
                List<String> opts = new ArrayList<>();
                opts.add(correct);
                opts.addAll(Arrays.asList(distractors));
                Collections.shuffle(opts, random);
                int correctIndex = opts.indexOf(correct);
                
                String[] formattedOptions = new String[4];
                for (int i = 0; i < 4; i++) {
                    formattedOptions[i] = opts.get(i);
                }
                
                return new Question(0, questionText, formattedOptions, correctIndex, null);
            }
        } catch (Exception e) {
            System.out.println("Error in generateMCQ: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private boolean isCommonWord(String word) {
        word = word.toLowerCase().replaceAll("[^a-z]", "");
        String[] common = {"the", "and", "for", "are", "but", "not", "you", "all", "can", "her", "was", "one", 
                          "our", "out", "day", "get", "has", "him", "his", "how", "man", "new", "now", "old", 
                          "see", "two", "way", "who", "boy", "did", "its", "let", "put", "say", "she", "too", "use"};
        for (String c : common) {
            if (word.equals(c)) return true;
        }
        return false;
    }
    
    private List<String> generateDistractors(String correctAnswer, String context) {
        List<String> distractors = new ArrayList<>();
        
        try {
            if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                distractors.add("Option A");
                distractors.add("Option B");
                distractors.add("Option C");
                return distractors;
            }
            
            // Domain-specific wrong answers based on common technical terms
            Map<String, String[]> domainTerms = new HashMap<>();
            domainTerms.put("java", new String[]{"Python", "JavaScript", "C++", "Ruby"});
            domainTerms.put("python", new String[]{"Java", "JavaScript", "Ruby", "PHP"});
            domainTerms.put("object", new String[]{"procedural", "functional", "declarative", "imperative"});
            domainTerms.put("oriented", new String[]{"based", "driven", "structured", "focused"});
            domainTerms.put("machine", new String[]{"human", "manual", "automated", "artificial"});
            domainTerms.put("learning", new String[]{"training", "teaching", "studying", "memorizing"});
            domainTerms.put("network", new String[]{"system", "protocol", "interface", "connection"});
            domainTerms.put("data", new String[]{"information", "content", "metadata", "records"});
            domainTerms.put("algorithm", new String[]{"heuristic", "formula", "function", "procedure"});
            domainTerms.put("memory", new String[]{"storage", "cache", "buffer", "register"});
            domainTerms.put("virtual", new String[]{"physical", "real", "actual", "concrete"});
            domainTerms.put("intelligence", new String[]{"automation", "computation", "processing", "analysis"});
            
            String lowerAnswer = correctAnswer.toLowerCase();
        
        // Strategy 1: Check for domain-specific alternatives
        for (String key : domainTerms.keySet()) {
            if (lowerAnswer.contains(key)) {
                String[] alternatives = domainTerms.get(key);
                for (int i = 0; i < Math.min(2, alternatives.length); i++) {
                    int randomIdx = random.nextInt(alternatives.length);
                    if (!distractors.contains(alternatives[randomIdx])) {
                        distractors.add(alternatives[randomIdx]);
                    }
                }
                break;
            }
        }
        
        // Strategy 2: Similar words with character variations
        if (correctAnswer.length() > 5 && distractors.size() < 3) {
            // Swap characters
            char[] chars = correctAnswer.toCharArray();
            if (chars.length > 2) {
                char temp = chars[0];
                chars[0] = chars[1];
                chars[1] = temp;
                distractors.add(new String(chars));
            }
        }
        
        // Strategy 3: Common wrong answers for specific types
        if (correctAnswer.matches(".*[0-9].*")) {
            // If it contains numbers, generate similar numbers
            distractors.add(correctAnswer.replaceAll("[0-9]", String.valueOf(random.nextInt(9) + 1)));
        }
        
        // Strategy 4: Opposite or contrasting terms
        Map<String, String> opposites = new HashMap<>();
        opposites.put("high", "low");
        opposites.put("fast", "slow");
        opposites.put("large", "small");
        opposites.put("simple", "complex");
        opposites.put("true", "false");
        opposites.put("correct", "incorrect");
        opposites.put("valid", "invalid");
        
        for (String key : opposites.keySet()) {
            if (lowerAnswer.contains(key)) {
                distractors.add(correctAnswer.replace(key, opposites.get(key)));
                break;
            }
        }
        
        // Strategy 5: Add generic but contextually wrong terms
        String[] genericWrong = {
            "None of the above",
            "All options are correct",
            "Cannot be determined",
            "Depends on context",
            "Not applicable",
            "Undefined behavior",
            "Random value",
            "System default"
        };
        
        // Fill remaining slots with unique distractors
        int attempts = 0;
        while (distractors.size() < 3 && attempts < 20) {
            String candidate;
            if (distractors.size() < 1) {
                // First distractor: try generic wrong answer
                candidate = genericWrong[random.nextInt(genericWrong.length)];
            } else if (distractors.size() < 2 && correctAnswer.length() > 4) {
                // Second distractor: modified correct answer
                if (random.nextBoolean()) {
                    candidate = correctAnswer.substring(0, correctAnswer.length() - 1) + "ed";
                } else {
                    candidate = "Non-" + correctAnswer.toLowerCase();
                }
            } else {
                // Third distractor: another variation
                candidate = correctAnswer.toLowerCase() + "ing";
            }
            
            if (!distractors.contains(candidate) && !candidate.equalsIgnoreCase(correctAnswer)) {
                distractors.add(candidate);
            }
            attempts++;
        }
        
        // Ensure we have at least 3 distractors - add simple ones if needed
        while (distractors.size() < 3) {
            String simple = "Option " + (char)('A' + distractors.size());
            if (!distractors.contains(simple)) {
                distractors.add(simple);
            }
        }
        
        // Return exactly 3 unique distractors
        List<String> unique = new ArrayList<>();
        for (String d : distractors) {
            if (!d.equalsIgnoreCase(correctAnswer) && unique.size() < 3) {
                if (!unique.contains(d)) {
                    unique.add(d);
                }
            }
        }
        
        // Final safety check
        if (unique.size() < 3) {
            for (int i = unique.size(); i < 3; i++) {
                unique.add("Wrong option " + (i + 1));
            }
        }
        
        return unique;
        
        } catch (Exception e) {
            System.out.println("Error in generateDistractors: " + e.getMessage());
            e.printStackTrace();
            // Return default distractors on error
            List<String> fallback = new ArrayList<>();
            fallback.add("Option A");
            fallback.add("Option B");
            fallback.add("Option C");
            return fallback;
        }
    }

    private Question generateTrueFalse(String sentence) {
        try {
            if (sentence == null || sentence.trim().isEmpty()) {
                return null;
            }
            String questionText = "True or False: " + sentence + ".";
            String[] options = new String[]{"True", "False"};
            int correctIndex = 0;
            return new Question(1, questionText, options, correctIndex, null);
        } catch (Exception e) {
            System.out.println("Error in generateTrueFalse: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Question generateFillBlank(String sentence) {
        try {
            if (sentence == null || sentence.trim().isEmpty()) {
                return null;
            }
            
            String[] words = sentence.split("\\s+");
            if (words.length == 0) {
                return null;
            }
            
            int idx = -1;
            int maxLen = 0;
            for (int i = 0; i < words.length; i++) {
                String w = words[i].replaceAll("[^a-zA-Z0-9]", "");
                if (w.length() > maxLen && w.length() >= 5) {
                    maxLen = w.length();
                    idx = i;
                }
            }
            if (idx == -1) return null;

            String answer = words[idx].replaceAll("[^a-zA-Z0-9]", "");
            words[idx] = "_____";
            String questionText = "Fill in the blank:\n" + String.join(" ", words) + ".";
            return new Question(2, questionText, null, -1, answer);
        } catch (Exception e) {
            System.out.println("Error in generateFillBlank: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ====== Timer ======
    private void startTimer() {
        stopTimer();
        quizTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                stopTimer();
                quizActive = false;
                JOptionPane.showMessageDialog(this, "Time's up!", "Quiz Ended", JOptionPane.INFORMATION_MESSAGE);
                endQuiz();
            }
        });
        quizTimer.start();
        updateTimerLabel();
    }

    private void stopTimer() {
        if (quizTimer != null) {
            quizTimer.stop();
            quizTimer = null;
        }
    }

    private void updateTimerLabel() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("⏱ Time: %02d:%02d", mins, secs));
        
        if (remainingSeconds <= 60) {
            timerLabel.setForeground(Color.RED);
        } else {
            timerLabel.setForeground(Color.WHITE);
        }
    }

    // ====== Results Management ======
    private void appendResultToCSV(int score, int total, double accuracy) {
        try {
            boolean newFile = !(new File(RESULTS_FILE)).exists();
            FileWriter fw = new FileWriter(RESULTS_FILE, true);
            PrintWriter pw = new PrintWriter(fw);
            if (newFile) {
                pw.println("username,category,difficulty,score,total,accuracy_percent,timestamp");
            }
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println(currentUser + "," + currentCategory + "," + currentDifficulty + "," +
                    score + "," + total + "," + String.format("%.2f", accuracy) + "," + ts);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════════\n");
        sb.append("                           QUIZ RESULTS DASHBOARD\n");
        sb.append("═══════════════════════════════════════════════════════════════════════════\n\n");

        File f = new File(RESULTS_FILE);
        if (!f.exists()) {
            sb.append("No results yet. Take a quiz and finish it to generate results.\n");
            resultsTextArea.setText(sb.toString());
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    sb.append(String.format("%-15s %-12s %-12s %-10s %-8s %-12s %s\n",
                            "USERNAME", "CATEGORY", "DIFFICULTY", "SCORE", "TOTAL", "ACCURACY", "TIMESTAMP"));
                    sb.append("───────────────────────────────────────────────────────────────────────────\n");
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    sb.append(String.format("%-15s %-12s %-12s %-10s %-8s %-12s %s\n",
                            parts[0], parts[1], parts[2], parts[3] + "/" + parts[4],
                            parts[4], parts[5] + "%", parts[6]));
                }
            }
            br.close();

            sb.append("\n═══════════════════════════════════════════════════════════════════════════\n");
        } catch (IOException e) {
            sb.append("Error reading results: ").append(e.getMessage());
        }

        resultsTextArea.setText(sb.toString());
        resultsTextArea.setCaretPosition(0);
    }

    // ====== PDF Export ======
    private void downloadPdfReport() {
        if (lastQuizResult == null) {
            JOptionPane.showMessageDialog(this, "No quiz result available to export.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        String defaultFileName = "Quiz_Report_" + currentUser + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        fileChooser.setSelectedFile(new File(defaultFileName));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
            }

            try {
                generatePDF(outputFile);
                JOptionPane.showMessageDialog(this,
                        "PDF report generated successfully!\n" + outputFile.getAbsolutePath(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error generating PDF: " + ex.getMessage(),
                        "PDF Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void generatePDF(File outputFile) throws Exception {
        // Generate HTML-based report that can be opened in any browser
        // This is more reliable than text-based PDF without external libraries
        
        String htmlContent = generateHTMLReport();
        
        // Save as HTML file (can be opened in browser and printed as PDF)
        String finalPath = outputFile.getAbsolutePath();
        if (finalPath.toLowerCase().endsWith(".pdf")) {
            finalPath = finalPath.substring(0, finalPath.length() - 4) + ".html";
        }
        
        try (FileWriter writer = new FileWriter(finalPath)) {
            writer.write(htmlContent);
        }
        
        // Open the HTML report in default browser automatically
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new File(finalPath).toURI());
        } catch (Exception e) {
            // If can't open automatically, just save the file
            System.out.println("Report saved to: " + finalPath);
        }
        
        JOptionPane.showMessageDialog(this,
            "Report generated successfully!\n\n" +
            "File saved as: " + finalPath + "\n\n" +
            "The report will open in your browser.\n" +
            "You can print it to PDF from the browser (Ctrl+P > Save as PDF)",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Quiz Report - ").append(lastQuizResult.username).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        html.append(".container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 900px; margin: auto; }\n");
        html.append("h1 { color: #3F51B5; text-align: center; border-bottom: 3px solid #3F51B5; padding-bottom: 20px; }\n");
        html.append("h2 { color: #FF5722; margin-top: 30px; }\n");
        html.append(".info-section { background: #E3F2FD; padding: 20px; border-radius: 5px; margin: 20px 0; }\n");
        html.append(".info-row { display: flex; margin: 10px 0; }\n");
        html.append(".info-label { font-weight: bold; width: 150px; color: #1976D2; }\n");
        html.append(".summary { background: #C8E6C9; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0; }\n");
        html.append(".score { font-size: 48px; font-weight: bold; color: #2E7D32; }\n");
        html.append(".accuracy { font-size: 32px; color: #558B2F; }\n");
        html.append(".grade { font-size: 28px; color: #33691E; margin-top: 10px; }\n");
        html.append(".question { background: #FFF9C4; border-left: 5px solid #F57C00; padding: 20px; margin: 20px 0; border-radius: 5px; }\n");
        html.append(".question.correct { border-left-color: #4CAF50; background: #E8F5E9; }\n");
        html.append(".question.incorrect { border-left-color: #F44336; background: #FFEBEE; }\n");
        html.append(".question-header { font-size: 18px; font-weight: bold; margin-bottom: 10px; }\n");
        html.append(".question-text { font-size: 16px; margin: 15px 0; line-height: 1.6; }\n");
        html.append(".options { margin: 15px 0; }\n");
        html.append(".option { padding: 10px; margin: 5px 0; background: white; border-radius: 5px; border: 1px solid #ddd; }\n");
        html.append(".user-answer { background: #BBDEFB; padding: 10px; border-radius: 5px; margin: 10px 0; }\n");
        html.append(".correct-answer { background: #C8E6C9; padding: 10px; border-radius: 5px; margin: 10px 0; }\n");
        html.append(".status-correct { color: #4CAF50; font-weight: bold; }\n");
        html.append(".status-incorrect { color: #F44336; font-weight: bold; }\n");
        html.append("@media print { body { margin: 0; background: white; } .container { box-shadow: none; } }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class='container'>\n");
        
        // Header
        html.append("<h1>INTELLIGENT QUIZ APPLICATION<br/>DETAILED REPORT</h1>\n");
        
        // User Info Section
        html.append("<div class='info-section'>\n");
        html.append("<div class='info-row'><span class='info-label'>User:</span><span>").append(lastQuizResult.username).append("</span></div>\n");
        html.append("<div class='info-row'><span class='info-label'>Category:</span><span>").append(lastQuizResult.category).append("</span></div>\n");
        html.append("<div class='info-row'><span class='info-label'>Difficulty:</span><span>").append(lastQuizResult.difficulty).append("</span></div>\n");
        html.append("<div class='info-row'><span class='info-label'>Date & Time:</span><span>").append(lastQuizResult.timestamp).append("</span></div>\n");
        html.append("</div>\n");
        
        // Performance Summary
        html.append("<div class='summary'>\n");
        html.append("<h2 style='margin-top:0;color:#2E7D32;'>PERFORMANCE SUMMARY</h2>\n");
        html.append("<div class='score'>").append(lastQuizResult.score).append(" / ").append(lastQuizResult.total).append("</div>\n");
        html.append("<div class='accuracy'>Accuracy: ").append(String.format("%.2f", lastQuizResult.accuracy)).append("%</div>\n");
        html.append("<div class='grade'>Grade: ").append(getGrade(lastQuizResult.accuracy)).append("</div>\n");
        html.append("</div>\n");
        
        // Detailed Questions
        html.append("<h2>DETAILED QUESTION-BY-QUESTION ANALYSIS</h2>\n");
        
        for (int i = 0; i < lastQuizResult.questions.size(); i++) {
            Question q = lastQuizResult.questions.get(i);
            String statusClass = q.isCorrect ? "correct" : "incorrect";
            String statusText = q.isCorrect ? "✓ CORRECT" : "✗ INCORRECT";
            String statusColor = q.isCorrect ? "status-correct" : "status-incorrect";
            
            html.append("<div class='question ").append(statusClass).append("'>\n");
            html.append("<div class='question-header'>Question ").append(i + 1).append(": ");
            html.append("<span class='").append(statusColor).append("'>").append(statusText).append("</span></div>\n");
            html.append("<div class='question-text'><strong>Q:</strong> ").append(escapeHtml(q.text)).append("</div>\n");
            
            // Show options if MCQ or True/False
            if (q.type == 0 || q.type == 1) {
                html.append("<div class='options'><strong>Options:</strong><br/>\n");
                for (int j = 0; j < q.options.length; j++) {
                    html.append("<div class='option'>").append(escapeHtml(q.options[j])).append("</div>\n");
                }
                html.append("</div>\n");
            }
            
            html.append("<div class='user-answer'><strong>Your Answer:</strong> ").append(escapeHtml(q.userAnswer)).append("</div>\n");
            
            if (!q.isCorrect) {
                String correctAns = (q.type == 2) ? q.correctText : q.options[q.correctIndex];
                html.append("<div class='correct-answer'><strong>Correct Answer:</strong> ").append(escapeHtml(correctAns)).append("</div>\n");
            }
            
            html.append("</div>\n");
        }
        
        // Footer
        html.append("<hr style='margin-top:40px;'/>\n");
        html.append("<p style='text-align:center;color:#666;'>Generated by Intelligent Quiz Application</p>\n");
        html.append("</div>\n</body>\n</html>");
        
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br/>");
    }

    private String getGrade(double accuracy) {
        if (accuracy >= 90) return "A+ (Excellent)";
        if (accuracy >= 80) return "A (Very Good)";
        if (accuracy >= 70) return "B (Good)";
        if (accuracy >= 60) return "C (Satisfactory)";
        if (accuracy >= 50) return "D (Pass)";
        return "F (Fail)";
    }

    // ====== Main Method ======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IntelligentQuizApp();
        });
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = "Guest";
            cardLayout.show(mainPanel, "login");
        }
    }
}
