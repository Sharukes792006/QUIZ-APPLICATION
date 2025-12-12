import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Intelligent Quiz App - AWT Only, Single File
 *
 * How to run (terminal / VS Code):
 * 1) Save as: IntelligentQuizAppAWT.java
 * 2) Compile: javac IntelligentQuizAppAWT.java
 * 3) Run   : java IntelligentQuizAppAWT        
 */
public class IntelligentQuizAppAWT {

    // ====== Inner model classes ======
    static class Question {
        // type: 0 = MCQ, 1 = TRUE/FALSE, 2 = FILL_IN_BLANK
        int type;
        String text;
        String[] options;   // for MCQ / True-False
        int correctIndex;   // for MCQ / True-False, -1 for fill in
        String correctText; // for FILL_IN_BLANK

        Question(int type, String text, String[] options, int correctIndex, String correctText) {
            this.type = type;
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
            this.correctText = correctText;
        }
    }

    static class ProgressBar extends Canvas {
        int total = 1;
        int current = 0;

        public void setProgress(int current, int total) {
            this.current = current;
            this.total = Math.max(1, total);
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            int w = getWidth() - 20;
            int h = 20;
            int x = 10;
            int y = 10;
            g.drawRect(x, y, w, h);
            int filled = (int) ((current / (float) total) * w);
            g.fillRect(x, y, filled, h);
            g.drawString(current + " / " + total, x + 5, y + h + 15);
        }
    }

    // ====== Main app fields ======
    private Frame frame;
    private CardLayout cardLayout;

    private Panel loginPanel;
    private Panel dashboardPanel;
    private Panel quizSetupPanel;
    private Panel quizPanel;
    private Panel summaryPanel;
    private Panel resultsPanel;

    // Login
    private TextField usernameField;
    private String currentUser = "Guest";

    // Dashboard label
    private Label welcomeLabel;

    // Quiz setup controls
    private Choice categoryChoice;
    private Choice difficultyChoice;
    private TextField timeLimitField;     // minutes
    private TextField numQuestionsField;
    private TextArea contentArea;
    private Label setupMessageLabel;

    // Quiz runtime
    private java.util.List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int totalQuestions = 0;
    private int remainingSeconds = 0;
    private Timer timer;
    private boolean quizActive = false;

    // Quiz panel controls
    private Label timerLabel;
    private Label questionCounterLabel;
    private TextArea questionTextArea;
    private Panel optionsPanel;
    private Label feedbackLabel;
    private Button nextButton;
    private ProgressBar progressBar;

    // For MCQ / True-False
    private CheckboxGroup optionGroup;
    // For fill in blank
    private TextField fillAnswerField;

    // Summary panel
    private Label summaryLabel;

    // Results storage
    private final String RESULTS_FILE = "quiz_results.csv";

    // Sample content for "intelligent" question generation
    private Map<String, String> sampleContent = new HashMap<>();

    // Random for question generator
    private Random random = new Random();

    // ====== Constructor ======
    public IntelligentQuizAppAWT() {
        initSampleContent();
        initFrameAndCards();
        initLoginPanel();
        initDashboardPanel();
        initQuizSetupPanel();
        initQuizPanel();
        initSummaryPanel();
        initResultsPanel();

        frame.setVisible(true);
    }

    // ====== Initialization ======
    private void initSampleContent() {
        sampleContent.put("Java",
                "Java is an object oriented programming language. " +
                "It runs on the Java Virtual Machine. " +
                "Classes and objects are core concepts. " +
                "Inheritance and polymorphism are supported. " +
                "The Java Standard Library provides collections and IO utilities.");

        sampleContent.put("AI",
                "Artificial Intelligence is the simulation of human intelligence in machines. " +
                "Machine learning is a subset of AI. " +
                "Neural networks are used for deep learning. " +
                "AI can be applied in vision and language tasks. " +
                "Training data quality affects model performance.");

        sampleContent.put("Networking",
                "Computer networking connects multiple computers together. " +
                "The TCP protocol provides reliable communication. " +
                "IP addressing uniquely identifies hosts. " +
                "Routers forward packets between networks. " +
                "The OSI model has seven layers.");
    }

    private void initFrameAndCards() {
        frame = new Frame("Intelligent Quiz App (AWT Only)");
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        frame.setLayout(cardLayout);

        // Window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (timer != null) timer.cancel();
                frame.dispose();
                System.exit(0);
            }
        });
    }

    private void initLoginPanel() {
        loginPanel = new Panel(new BorderLayout());

        Label title = new Label("Intelligent Quiz App", Label.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        loginPanel.add(title, BorderLayout.NORTH);

        Panel center = new Panel(new GridLayout(3, 1, 10, 10));
        center.setBackground(Color.lightGray);

        Label info = new Label("Login to continue", Label.CENTER);
        center.add(info);

        Panel userPanel = new Panel(new FlowLayout());
        userPanel.add(new Label("Username: "));
        usernameField = new TextField(20);
        userPanel.add(usernameField);
        center.add(userPanel);

        Button loginButton = new Button("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String u = usernameField.getText().trim();
                if (!u.isEmpty()) {
                    currentUser = u;
                } else {
                    currentUser = "Guest";
                }
                welcomeLabel.setText("Welcome, " + currentUser + "!");
                cardLayout.show(frame, "dashboard");
            }
        });
        Panel btnPanel = new Panel(new FlowLayout());
        btnPanel.add(loginButton);
        center.add(btnPanel);

        loginPanel.add(center, BorderLayout.CENTER);

        frame.add(loginPanel, "login");
    }

    private void initDashboardPanel() {
        dashboardPanel = new Panel(new BorderLayout());

        welcomeLabel = new Label("Welcome!", Label.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dashboardPanel.add(welcomeLabel, BorderLayout.NORTH);

        Panel center = new Panel(new GridLayout(3, 1, 20, 20));
        center.setBackground(new Color(220, 220, 255));

        Button createStartButton = new Button("Create & Start Quiz");
        createStartButton.setFont(new Font("Dialog", Font.BOLD, 16));
        createStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupMessageLabel.setText("Paste content or use sample content.");
                cardLayout.show(frame, "setup");
            }
        });

        Button viewResultsButton = new Button("View Results Dashboard");
        viewResultsButton.setFont(new Font("Dialog", Font.BOLD, 16));
        viewResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadResults();
                cardLayout.show(frame, "results");
            }
        });

        Button exitButton = new Button("Exit");
        exitButton.setFont(new Font("Dialog", Font.BOLD, 16));
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (timer != null) timer.cancel();
                frame.dispose();
                System.exit(0);
            }
        });

        center.add(createStartButton);
        center.add(viewResultsButton);
        center.add(exitButton);

        dashboardPanel.add(center, BorderLayout.CENTER);

        frame.add(dashboardPanel, "dashboard");
    }

    private void initQuizSetupPanel() {
        quizSetupPanel = new Panel(new BorderLayout());

        Label header = new Label("Create Quiz from Learning Material", Label.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        quizSetupPanel.add(header, BorderLayout.NORTH);

        Panel formPanel = new Panel(new GridLayout(5, 2, 10, 10));

        // Category
        formPanel.add(new Label("Category: "));
        categoryChoice = new Choice();
        categoryChoice.add("Java");
        categoryChoice.add("AI");
        categoryChoice.add("Networking");
        categoryChoice.add("Custom");
        formPanel.add(categoryChoice);

        // Difficulty
        formPanel.add(new Label("Difficulty: "));
        difficultyChoice = new Choice();
        difficultyChoice.add("Easy");
        difficultyChoice.add("Medium");
        difficultyChoice.add("Hard");
        formPanel.add(difficultyChoice);

        // Time limit
        formPanel.add(new Label("Time limit (minutes): "));
        timeLimitField = new TextField("2");
        formPanel.add(timeLimitField);

        // Number of questions
        formPanel.add(new Label("Number of questions: "));
        numQuestionsField = new TextField("5");
        formPanel.add(numQuestionsField);

        // Upload TXT file button
        formPanel.add(new Label("Upload TXT File: "));
        Button uploadButton = new Button("Browse & Upload");
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadTextFile();
            }
        });
        formPanel.add(uploadButton);

        Panel topCenter = new Panel(new BorderLayout());
        topCenter.add(formPanel, BorderLayout.NORTH);

        setupMessageLabel = new Label("Paste content or leave empty to use sample for selected category.", Label.CENTER);
        topCenter.add(setupMessageLabel, BorderLayout.SOUTH);

        quizSetupPanel.add(topCenter, BorderLayout.CENTER);

        // Content area
        contentArea = new TextArea("", 10, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        quizSetupPanel.add(contentArea, BorderLayout.SOUTH);

        // Bottom buttons
        Panel bottom = new Panel(new FlowLayout());
        Button backButton = new Button("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(frame, "dashboard");
            }
        });

        Button startButton = new Button("Generate & Start Quiz");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startQuizFromSetup();
            }
        });

        bottom.add(backButton);
        bottom.add(startButton);
        quizSetupPanel.add(bottom, BorderLayout.NORTH);

        frame.add(quizSetupPanel, "setup");
    }

    private void initQuizPanel() {
        quizPanel = new Panel(new BorderLayout());

        // Top area: timer + question counter
        Panel top = new Panel(new BorderLayout());
        timerLabel = new Label("Time: 00:00", Label.LEFT);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        top.add(timerLabel, BorderLayout.WEST);

        questionCounterLabel = new Label("Question 0 / 0", Label.RIGHT);
        top.add(questionCounterLabel, BorderLayout.EAST);

        quizPanel.add(top, BorderLayout.NORTH);

        // Center: question + options + progress bar
        Panel center = new Panel(new BorderLayout());

        questionTextArea = new TextArea("", 5, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
        questionTextArea.setEditable(false);
        center.add(questionTextArea, BorderLayout.NORTH);

        optionsPanel = new Panel();
        optionsPanel.setLayout(new GridLayout(5, 1, 5, 5));
        center.add(optionsPanel, BorderLayout.CENTER);

        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new Dimension(400, 40));
        center.add(progressBar, BorderLayout.SOUTH);

        quizPanel.add(center, BorderLayout.CENTER);

        // Bottom: feedback + buttons
        Panel bottom = new Panel(new BorderLayout());

        feedbackLabel = new Label("Select an answer and click Next.", Label.CENTER);
        bottom.add(feedbackLabel, BorderLayout.CENTER);

        Panel btnPanel = new Panel(new FlowLayout());
        nextButton = new Button("Next");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleNextQuestion();
            }
        });
        btnPanel.add(nextButton);

        Button cancelButton = new Button("Cancel Quiz");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopTimer();
                quizActive = false;
                setupMessageLabel.setText("Quiz cancelled.");
                cardLayout.show(frame, "dashboard");
            }
        });
        btnPanel.add(cancelButton);

        bottom.add(btnPanel, BorderLayout.EAST);

        quizPanel.add(bottom, BorderLayout.SOUTH);

        frame.add(quizPanel, "quiz");
    }

    private void initSummaryPanel() {
        summaryPanel = new Panel(new BorderLayout());

        Label header = new Label("Quiz Summary", Label.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        summaryPanel.add(header, BorderLayout.NORTH);

        summaryLabel = new Label("Summary goes here", Label.CENTER);
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout());
        Button backButton = new Button("Back to Dashboard");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(frame, "dashboard");
            }
        });

        Button viewResultsButton = new Button("View Results File");
        viewResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadResults();
                cardLayout.show(frame, "results");
            }
        });

        bottom.add(backButton);
        bottom.add(viewResultsButton);
        summaryPanel.add(bottom, BorderLayout.SOUTH);

        frame.add(summaryPanel, "summary");
    }

    private TextArea resultsArea;

    private void initResultsPanel() {
        resultsPanel = new Panel(new BorderLayout());

        Label header = new Label("Results Dashboard (CSV view)", Label.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        resultsPanel.add(header, BorderLayout.NORTH);

        resultsArea = new TextArea("", 20, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        resultsArea.setEditable(false);
        resultsPanel.add(resultsArea, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout());
        Button backButton = new Button("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(frame, "dashboard");
            }
        });
        bottom.add(backButton);

        resultsPanel.add(bottom, BorderLayout.SOUTH);

        frame.add(resultsPanel, "results");
    }

    // ====== File Upload Logic ======
    private void uploadTextFile() {
        FileDialog fd = new FileDialog(frame, "Select a TXT file", FileDialog.LOAD);
        fd.setFile("*.txt");
        fd.setVisible(true);
        
        String directory = fd.getDirectory();
        String filename = fd.getFile();
        
        if (directory == null || filename == null) {
            setupMessageLabel.setText("No file selected.");
            return;
        }
        
        String filePath = directory + filename;
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                sb.append(line).append(" ");
            }
            br.close();
            
            String fileContent = sb.toString().trim();
            
            if (fileContent.isEmpty()) {
                setupMessageLabel.setText("The selected file is empty. Please choose a file with content.");
            } else {
                contentArea.setText(fileContent);
                setupMessageLabel.setText("File uploaded successfully: " + filename);
            }
            
        } catch (IOException ex) {
            setupMessageLabel.setText("Error reading file: " + ex.getMessage());
        }
    }

    // ====== Quiz Setup Logic ======
    private void startQuizFromSetup() {
        String category = categoryChoice.getSelectedItem();
        String difficulty = difficultyChoice.getSelectedItem();
        int numQ;
        int minutes;

        try {
            numQ = Integer.parseInt(numQuestionsField.getText().trim());
            if (numQ <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            setupMessageLabel.setText("Invalid number of questions. Please enter a positive integer.");
            return;
        }

        try {
            minutes = Integer.parseInt(timeLimitField.getText().trim());
            if (minutes <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            setupMessageLabel.setText("Invalid time limit. Please enter a positive integer.");
            return;
        }

        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            if (!category.equals("Custom")) {
                content = sampleContent.get(category);
                setupMessageLabel.setText("Using built-in sample content for " + category + ".");
            } else {
                setupMessageLabel.setText("Please paste some content for Custom category.");
                return;
            }
        }

        currentQuestions = generateQuestions(content, numQ, category, difficulty);
        if (currentQuestions.isEmpty()) {
            setupMessageLabel.setText("Could not generate questions from content. Try adding more sentences.");
            return;
        }

        totalQuestions = currentQuestions.size();
        currentQuestionIndex = 0;
        correctCount = 0;

        remainingSeconds = minutes * 60;

        startTimer();
        quizActive = true;

        showCurrentQuestion();
        cardLayout.show(frame, "quiz");
    }

    // ====== Question Generation Logic (Simulated "Intelligent") ======
    private java.util.List<Question> generateQuestions(String content, int maxQuestions,
                                                       String category, String difficulty) {
        java.util.List<Question> questions = new ArrayList<>();

        // Split into sentences
        String[] sentences = content.split("[\\.\\?!]");
        java.util.List<String> cleaned = new ArrayList<>();
        for (String s : sentences) {
            String t = s.trim();
            if (t.split("\\s+").length >= 4) {
                cleaned.add(t);
            }
        }

        if (cleaned.isEmpty()) return questions;

        // We'll generate 3 types: MCQ, True/False, Fill in Blank
        int idx = 0;
        while (questions.size() < maxQuestions && !cleaned.isEmpty()) {
            String sentence = cleaned.get(idx % cleaned.size());
            int type = questions.size() % 3; // rotate types

            if (type == 0) {
                // MCQ: Use sentence part before " is " as question subject
                questions.add(generateMCQ(sentence));
            } else if (type == 1) {
                // True/False: Simple statement -> True
                questions.add(generateTrueFalse(sentence));
            } else {
                // Fill in the blank
                Question q = generateFillBlank(sentence);
                if (q != null) {
                    questions.add(q);
                } else {
                    // fall back to True/False
                    questions.add(generateTrueFalse(sentence));
                }
            }
            idx++;
        }

        // Shuffle
        Collections.shuffle(questions, random);
        if (questions.size() > maxQuestions) {
            questions = questions.subList(0, maxQuestions);
        }
        return questions;
    }

    private Question generateMCQ(String sentence) {
        String questionText = "What is the correct completion of this statement?\n" + sentence + ".";
        // Very simple MCQ: 1 correct + 3 distractors from category-related words
        String correct = "Correct";
        String[] distractors = new String[]{"Incorrect A", "Incorrect B", "Incorrect C"};

        // Shuffle answers
        java.util.List<String> opts = new ArrayList<>();
        opts.add(correct);
        opts.addAll(Arrays.asList(distractors));
        Collections.shuffle(opts, random);
        int correctIndex = opts.indexOf(correct);

        return new Question(0, questionText, opts.toArray(new String[0]), correctIndex, null);
    }

    private Question generateTrueFalse(String sentence) {
        String questionText = "True or False: " + sentence + ".";
        String[] options = new String[]{"True", "False"};
        int correctIndex = 0; // Assume statement is true
        return new Question(1, questionText, options, correctIndex, null);
    }

    private Question generateFillBlank(String sentence) {
        String[] words = sentence.split("\\s+");
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
    }

    // ====== Timer Logic ======
    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                remainingSeconds--;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateTimerLabel();
                    }
                });
                if (remainingSeconds <= 0) {
                    timer.cancel();
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            quizActive = false;
                            feedbackLabel.setText("Time is up!");
                            endQuiz();
                        }
                    });
                }
            }
        }, 1000, 1000);
        updateTimerLabel();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateTimerLabel() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", mins, secs));
    }

    // ====== Quiz Flow ======
    private void showCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= currentQuestions.size()) return;

        Question q = currentQuestions.get(currentQuestionIndex);
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + " / " + currentQuestions.size());
        questionTextArea.setText(q.text);

        optionsPanel.removeAll();
        feedbackLabel.setText("Select an answer and click Next.");

        optionGroup = null;
        fillAnswerField = null;

        if (q.type == 0 || q.type == 1) {
            // MCQ or True/False
            optionGroup = new CheckboxGroup();
            for (int i = 0; i < q.options.length; i++) {
                Checkbox cb = new Checkbox(q.options[i], optionGroup, false);
                optionsPanel.add(cb);
            }
        } else if (q.type == 2) {
            // Fill in blank
            optionsPanel.setLayout(new GridLayout(2, 1, 5, 5));
            optionsPanel.add(new Label("Your answer:"));
            fillAnswerField = new TextField(30);
            optionsPanel.add(fillAnswerField);
        }

        optionsPanel.validate();
        optionsPanel.repaint();

        progressBar.setProgress(currentQuestionIndex + 1, currentQuestions.size());
    }

    private void handleNextQuestion() {
        if (!quizActive) {
            feedbackLabel.setText("Quiz not active.");
            return;
        }

        if (!evaluateCurrentAnswer()) {
            feedbackLabel.setText("Please select / enter an answer before continuing.");
            return;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= currentQuestions.size()) {
            endQuiz();
        } else {
            showCurrentQuestion();
        }
    }

    private boolean evaluateCurrentAnswer() {
        Question q = currentQuestions.get(currentQuestionIndex);

        if (q.type == 0 || q.type == 1) {
            if (optionGroup == null) return false;
            Checkbox selected = optionGroup.getSelectedCheckbox();
            if (selected == null) return false;

            String selectedText = selected.getLabel();
            int selectedIndex = -1;
            for (int i = 0; i < q.options.length; i++) {
                if (q.options[i].equals(selectedText)) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex == -1) return false;

            if (selectedIndex == q.correctIndex) {
                correctCount++;
                feedbackLabel.setText("Correct!");
            } else {
                feedbackLabel.setText("Incorrect. Correct answer: " + q.options[q.correctIndex]);
            }
            return true;
        } else {
            if (fillAnswerField == null) return false;
            String ans = fillAnswerField.getText().trim();
            if (ans.isEmpty()) return false;

            if (ans.equalsIgnoreCase(q.correctText)) {
                correctCount++;
                feedbackLabel.setText("Correct!");
            } else {
                feedbackLabel.setText("Incorrect. Correct answer: " + q.correctText);
            }
            return true;
        }
    }

    private void endQuiz() {
        stopTimer();
        quizActive = false;
        double accuracy = (correctCount * 100.0) / totalQuestions;
        String summary = "User: " + currentUser +
                " | Score: " + correctCount + " / " + totalQuestions +
                " | Accuracy: " + String.format("%.1f", accuracy) + "%";
        summaryLabel.setText(summary);

        appendResultToCSV(correctCount, totalQuestions, accuracy);

        cardLayout.show(frame, "summary");
    }

    // ====== Results / CSV ======
    private void appendResultToCSV(int score, int total, double accuracy) {
        try {
            boolean newFile = !(new File(RESULTS_FILE)).exists();
            FileWriter fw = new FileWriter(RESULTS_FILE, true);
            PrintWriter pw = new PrintWriter(fw);
            if (newFile) {
                pw.println("username,score,total,accuracy_percent,timestamp");
            }
            String ts = new Date().toString();
            pw.println(currentUser + "," + score + "," + total + "," +
                    String.format("%.1f", accuracy) + "," + ts);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            // ignore errors in demo
        }
    }

    private void loadResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("Results (from ").append(RESULTS_FILE).append(")\n\n");
        File f = new File(RESULTS_FILE);
        if (!f.exists()) {
            sb.append("No results yet. Take a quiz and finish it to generate results.\n");
            resultsArea.setText(sb.toString());
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            sb.append("Error reading results: ").append(e.getMessage());
        }
        resultsArea.setText(sb.toString());
    }

    // ====== Main ======
    public static void main(String[] args) {
        new IntelligentQuizAppAWT();
    }
}
