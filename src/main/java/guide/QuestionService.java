package guide;

import games.GameType;
import guide.param.Question;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.JSONUtils;

import java.util.*;

public class QuestionService {

    private Map<String, Question> questions;

    public QuestionService(GameType gameType) {
        this.questions = convert(JSONUtils.loadJSONFile("data/questions/" + gameType.name() + ".json"));
    }

    public static Map<String, Question> convert(JSONObject jsonObj) {
        Map<String, Question> questions = new HashMap<>();
        Set<Map.Entry<String, JSONObject>> entries = jsonObj.entrySet();

        for (Map.Entry<String, JSONObject> entry : entries) {
            JSONObject questionObj = entry.getValue();
            String type = (String) questionObj.get("type");
            String questionText = (String) questionObj.get("questionText");
            String answer = (String) questionObj.get("answer");
            JSONArray optionsArray = (JSONArray) questionObj.get("options");
            List<Question.Option> options = new ArrayList<>();

            for (Object obj : optionsArray) {
                JSONObject optionObj = (JSONObject) obj;
                String option = (String) optionObj.get("option");
                String optionText = (String) optionObj.get("optionText");
                options.add(new Question.Option(option, optionText));
            }

            questions.put(entry.getKey(), new Question(type, questionText, options, answer));
        }
        return questions;
    }

    public Map<String, Question> getQuestions() {
        return questions;
    }
}
