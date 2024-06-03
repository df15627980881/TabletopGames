package guide.param;

import java.util.List;

public class Question {

    private String type;

    private String questionText;

    private List<Option> options;


    private String answer;

    public Question() {
    }

    public Question(String type, String questionText, List<Option> options, String answer) {
        this.type = type;
        this.questionText = questionText;
        this.options = options;
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Question{" +
                "type='" + type + '\'' +
                ", questionText='" + questionText + '\'' +
                ", options=" + options +
                ", answer='" + answer + '\'' +
                '}';
    }

    public String toString(String questionNo) {
        return questionNo + ". " + questionText;
    }

    public static class Option {

        private String option;

        private String optionText;

        public Option(String option, String optionText) {
            this.option = option;
            this.optionText = optionText;
        }

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public String getOptionText() {
            return optionText;
        }

        public void setOptionText(String optionText) {
            this.optionText = optionText;
        }

        @Override
        public String toString() {
            return this.option + ". " + optionText;
        }
    }
}
