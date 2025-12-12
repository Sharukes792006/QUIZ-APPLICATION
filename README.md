# Intelligent Quiz Application - Enhanced Swing Version

## 🎯 Overview
A modern, interactive quiz application built with Java Swing that generates intelligent questions from uploaded text files and provides comprehensive PDF reports.

## ✨ Key Features

### 1. **Modern Swing UI**
- Professional Material Design-inspired interface
- Color-coded cards and buttons for intuitive navigation
- Smooth transitions between screens
- Responsive layouts with proper spacing

### 2. **File Upload Functionality**
- **Browse & Upload** button for easy TXT file selection
- Native file chooser dialog (JFileChooser)
- Supports .txt files
- Real-time feedback on file upload status
- File name displayed on button after successful upload

### 3. **Intelligent Question Generation**
Three types of questions automatically generated:
- **Multiple Choice Questions (MCQ)** - 4 options with one correct answer
- **True/False** - Binary choice questions
- **Fill in the Blank** - Tests specific knowledge

### 4. **Interactive Quiz Taking**
- Real-time timer with countdown
- Visual progress bar showing quiz completion
- Question counter (e.g., "Question 3 / 5")
- Instant feedback (Correct/Incorrect) after each answer
- Color-coded feedback (Green for correct, Red for incorrect)

### 5. **PDF Report Export**
- **Download PDF Report** button on summary screen
- Comprehensive report includes:
  - User information
  - Quiz metadata (category, difficulty, timestamp)
  - Performance summary with grade
  - Question-by-question analysis
  - User answers vs. correct answers
- Custom filename with timestamp
- Save anywhere on your system

### 6. **Results Dashboard**
- View all quiz history in organized table format
- CSV storage for data persistence
- Shows: Username, Category, Difficulty, Score, Accuracy, Timestamp
- Refresh functionality

### 7. **User Experience Enhancements**
- Login screen with personalized welcome
- Dashboard with card-based navigation
- Configurable quiz parameters:
  - Category selection (Java, AI, Networking, Custom)
  - Difficulty levels (Easy, Medium, Hard)
  - Time limit (1-60 minutes)
  - Number of questions (1-20)
- Cancel quiz option with confirmation dialog
- Hover effects on buttons and cards

## 🚀 How to Run

### Compilation
```bash
javac IntelligentQuizApp.java
```

### Execution
```bash
java IntelligentQuizApp
```

## 📖 How to Use

### Step 1: Login
1. Enter your username
2. Click "Login" button
3. Redirected to dashboard

### Step 2: Create Quiz
1. Click "Create & Start Quiz" card
2. Select category from dropdown
3. Choose difficulty level
4. Set time limit (in minutes)
5. Set number of questions
6. **Upload Text File**: Click "📁 Browse & Upload TXT File" button
   - Select a .txt file from your computer
   - File content will automatically populate in the text area
   - Or manually paste content instead
7. Click "Generate & Start Quiz →"

### Step 3: Take Quiz
1. Read each question carefully
2. Select your answer (radio button for MCQ/True-False, text field for Fill-in-Blank)
3. Click "Next Question →"
4. See immediate feedback (Correct/Incorrect)
5. Progress bar shows completion status
6. Timer counts down in top-left corner

### Step 4: View Results
1. Quiz summary appears automatically after completion
2. Review detailed performance metrics:
   - Overall score and accuracy
   - Grade (A+, A, B, C, D, F)
   - Question-by-question breakdown
3. **Download PDF**: Click "📥 Download PDF Report"
   - Choose save location
   - Enter filename (or use default with timestamp)
   - Click "Save"
4. View Results Dashboard for historical data

## 📁 File Structure

```
NEW/
├── IntelligentQuizApp.java          # Main application (Swing version)
├── IntelligentQuizAppAWT.java       # Legacy AWT version
├── IntelligentQuizApp.class         # Compiled main class
├── IntelligentQuizApp$*.class       # Inner class files
├── quiz_results.csv                  # Results database
└── Quiz_Report_*.pdf                 # Generated PDF reports
```

## 🎨 UI Components

### Color Scheme
- **Primary Color**: Blue (#3F51B5) - Headers and main buttons
- **Accent Color**: Orange (#FF5722) - Upload and special actions
- **Success Color**: Green (#4CAF50) - Positive feedback
- **Error Color**: Red (#F44336) - Errors and negative feedback
- **Background**: Light Gray (#F5F5F5) - Clean, modern look

### Screens
1. **Login Screen** - Username entry
2. **Dashboard** - Main navigation hub
3. **Quiz Setup** - Configuration and file upload
4. **Quiz Panel** - Active quiz taking
5. **Summary** - Results and PDF export
6. **Results Dashboard** - Historical data view

## 📊 Data Storage

### CSV Format (quiz_results.csv)
```
username,category,difficulty,score,total,accuracy_percent,timestamp
John,Java,Medium,4,5,80.00,2025-12-10 14:30:45
```

### PDF Report Contents
- Header with report title
- User and quiz information
- Performance summary with grade
- Detailed question analysis
- Each question with user answer and correct answer
- Generation timestamp

## 🔧 Technical Details

### Technologies Used
- **Java Swing** - Modern GUI framework
- **AWT** - Graphics and events
- **Java I/O** - File operations
- **Collections Framework** - Data management
- **Date/Time API** - Timestamp handling

### Key Classes
- `IntelligentQuizApp` - Main JFrame application
- `Question` - Question model (type, text, options, answers)
- `QuizResult` - Result model with all quiz data
- Custom UI components (buttons, cards, panels)

### Question Generation Algorithm
1. Parse uploaded text into sentences
2. Filter sentences (minimum 4 words)
3. Generate different question types:
   - MCQ: Create distractors
   - True/False: Convert to statement
   - Fill-in-Blank: Replace longest word
4. Shuffle questions for randomization

## 🆚 Comparison: AWT vs Swing Version

| Feature | AWT Version | Swing Version |
|---------|-------------|---------------|
| UI Framework | java.awt | javax.swing |
| Look & Feel | Basic/Native | Modern/Customizable |
| File Upload | FileDialog | JFileChooser |
| Components | Basic | Rich (JSpinner, JProgressBar) |
| Layout | Simple | Advanced with borders |
| Colors | Limited | Full customization |
| PDF Export | ❌ No | ✅ Yes |
| Hover Effects | ❌ No | ✅ Yes |
| Card Layout | Basic | Polished with animations |

## 💡 Tips

1. **Best Text Files**: Upload files with multiple complete sentences (at least 7-10 sentences)
2. **Question Quality**: Longer, information-rich sentences generate better questions
3. **Time Management**: Start with 2-3 minutes for 5 questions
4. **PDF Reports**: Save in a dedicated folder for easy tracking
5. **Custom Category**: Use when uploading specialized content

## 🐛 Troubleshooting

### Application won't start
```bash
# Make sure you compiled first
javac IntelligentQuizApp.java

# Check Java version (requires Java 8+)
java -version
```

### File upload not working
- Ensure file is .txt format
- Check file has read permissions
- Verify file contains text content

### PDF not generating
- Check write permissions in selected directory
- Ensure sufficient disk space
- Try using default filename

## 📝 Future Enhancements

Potential features for future versions:
- Multiple file format support (PDF, DOCX, etc.)
- Advanced PDF with charts and graphs (requires iText library)
- Question difficulty based on content analysis
- Export to Excel format
- User authentication and profiles
- Online quiz sharing
- Question bank management
- Timer per question option
- Hint system

## 📄 License

This is an educational project. Feel free to use and modify for learning purposes.

## 👨‍💻 Author

Created as an intelligent quiz generation system with modern UI and comprehensive reporting features.

---

**Enjoy creating and taking quizzes! 🎓✨**
