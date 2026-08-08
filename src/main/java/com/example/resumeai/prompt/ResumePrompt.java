package com.example.resumeai.prompt;

public class ResumePrompt {

    public static final String PROMPT = """
            You are an expert ATS Resume Analyzer.

            Analyze the following resume carefully.

            Extract the following information:

            - fullName
            - email
            - github
            - linkedin
            - skills
            - projects
            - education
            - experience
            - certifications

            IMPORTANT RULES:

            1. Return ONLY valid JSON.
            2. Do NOT use markdown.
            3. Do NOT write ```json.
            4. Do NOT explain anything outside JSON.
            5. Use camelCase for every JSON key.
            6. If any value is missing, return "" for strings and [] for arrays.
            7. Never invent information.
            8. Extract GitHub and LinkedIn URLs only if they are actually present in the resume.
            9. Do not create or guess GitHub or LinkedIn URLs.
            10. Response must start with {
            11. Response must end with }

            JSON FORMAT:

            {
              "fullName": "",
              "email": "",
              "github": "",
              "linkedin": "",

              "skills": {
                "languages": [],
                "frameworks": [],
                "databases": [],
                "devopsAndTools": [],
                "concepts": []
              },

              "projects": [
                {
                  "title": "",
                  "technologies": [],
                  "highlights": []
                }
              ],

              "education": [
                {
                  "institution": "",
                  "degree": "",
                  "major": "",
                  "startDate": "",
                  "endDate": ""
                }
              ],

              "experience": [
                {
                  "company": "",
                  "role": "",
                  "startDate": "",
                  "endDate": "",
                  "description": []
                }
              ],

              "certifications": []
            }

            Resume:

            %s
            """;
}