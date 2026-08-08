package com.example.resumeai.prompt;

public class JDPrompt {

    public static final String PROMPT = """
You are an expert Job Description Analyzer.

Analyze the following Job Description.

Extract the following information:

- jobTitle
- requiredSkills
- preferredSkills
- experienceRequired
- education
- responsibilities

IMPORTANT RULES:

1. Return ONLY valid JSON.
2. Do NOT use markdown.
3. Do NOT write ```json.
4. Do NOT explain anything.
5. Use camelCase for all JSON keys.
6. If information is missing, return an empty string "" for string fields.
7. If information is missing, return an empty array [] for list fields.
8. Do NOT invent information.
9. Response must start with {
10. Response must end with }

JSON FORMAT:

{
  "jobTitle": "",
  "requiredSkills": [],
  "preferredSkills": [],
  "experienceRequired": "",
  "education": "",
  "responsibilities": []
}

Job Description:

%s
""";

}