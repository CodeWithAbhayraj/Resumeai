package com.example.resumeai.ai;

import com.example.resumeai.dto.EducationDTO;
import com.example.resumeai.dto.ExperienceDTO;
import com.example.resumeai.dto.ImprovedResumeDTO;
import com.example.resumeai.dto.ProjectDTO;
import com.example.resumeai.dto.SkillsDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;


import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PdfAgent {

    private static final float MARGIN = 40;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private float yPosition;

    public byte[] generateResumePdf(ImprovedResumeDTO resume) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(document, page);

            yPosition = PAGE_HEIGHT - MARGIN;

            // =========================
            // PERSONAL INFORMATION
            // =========================

            writeCentered(
                    content,
                    safe(resume.getFullName()),
                    20,
                    true
            );

            yPosition -= 22;

            writeContactInfo(
                    document,
                    page,
                    content,
                    resume.getEmail(),
                    resume.getGithub(),
                    resume.getLinkedin()
            );

            yPosition -= 25;

            // =========================
            // PROFESSIONAL SUMMARY
            // =========================

            writeSectionTitle(content, "SUMMARY");

            writeWrappedText(
                    content,
                    resume.getProfessionalSummary(),
                    10
            );

            yPosition -= 10;

            // =========================
            // SKILLS
            // =========================

            writeSectionTitle(content, "SKILLS");

            SkillsDTO skills = resume.getSkills();

            if (skills != null) {

                writeSkillLine(
                        content,
                        "Languages",
                        skills.getLanguages()
                );

                writeSkillLine(
                        content,
                        "Frameworks",
                        skills.getFrameworks()
                );

                writeSkillLine(
                        content,
                        "Databases",
                        skills.getDatabases()
                );

                writeSkillLine(
                        content,
                        "DevOps & Tools",
                        skills.getDevopsAndTools()
                );

                writeSkillLine(
                        content,
                        "Concepts",
                        skills.getConcepts()
                );
            }

            yPosition -= 8;

            // =========================
            // PROJECTS
            // =========================

            writeSectionTitle(content, "PROJECTS");

            yPosition -= 2;

            if (resume.getProjects() != null) {

                for (ProjectDTO project : resume.getProjects()) {

                    writeBoldText(
                            content,
                            safe(project.getTitle()),
                            11
                    );

                    if (project.getTechnologies() != null) {

                        writeWrappedText(
                                content,
                                "Technologies: "
                                        + String.join(
                                        ", ",
                                        project.getTechnologies()
                                ),
                                9
                        );
                    }

                    if (project.getHighlights() != null) {

                        for (String highlight :
                                project.getHighlights()) {

                            writeBullet(
                                    content,
                                    highlight
                            );
                        }
                    }

                    yPosition -= 8;
                }
            }

            // =========================
            // EDUCATION
            // =========================

            writeSectionTitle(content, "EDUCATION");

            if (resume.getEducation() != null) {

                for (EducationDTO education :
                        resume.getEducation()) {

                    writeBoldText(
                            content,
                            safe(education.getDegree()),
                            10
                    );

                    writeWrappedText(
                            content,
                            safe(education.getInstitution()),
                            9
                    );

                    String dates =
                            safe(education.getStartDate())
                                    + " - "
                                    + safe(education.getEndDate());

                    writeWrappedText(
                            content,
                            dates,
                            9
                    );

                    yPosition -= 8;
                }
            }

// =========================
// EXPERIENCE
// =========================

            if (resume.getExperience() != null
                    && !resume.getExperience().isEmpty()) {

                writeSectionTitle(content, "EXPERIENCE");

                for (ExperienceDTO experience :
                        resume.getExperience()) {

                    writeBoldText(
                            content,
                            safe(experience.getRole())
                                    + " - "
                                    + safe(experience.getCompany()),
                            10
                    );

                    if (experience.getDuration() != null
                            && !experience.getDuration().isBlank()) {

                        writeWrappedText(
                                content,
                                experience.getDuration(),
                                9
                        );
                    }

                    if (experience.getDescription() != null
                            && !experience.getDescription().isBlank()) {

                        writeBullet(
                                content,
                                experience.getDescription()
                        );
                    }
                    yPosition -= 8;
                }
            }

            // =========================
            // CERTIFICATIONS
            // =========================

            writeSectionTitle(content, "CERTIFICATIONS");

            if (resume.getCertifications() != null) {

                for (String certification :
                        resume.getCertifications()) {

                    writeBullet(
                            content,
                            certification
                    );
                }
            }

            content.close();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to generate resume PDF.",
                    e
            );
        }

    }

    // =====================================================
    // SECTION TITLE
    // =====================================================

    private void writeSectionTitle(
            PDPageContentStream content,
            String title
    ) throws IOException {

        if (title == null || title.isBlank()) {
            return;
        }

        yPosition -= 4;

        content.beginText();

        content.setFont(
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                ),
                11
        );

        content.newLineAtOffset(
                MARGIN,
                yPosition
        );

        content.showText(clean(title.toUpperCase()));

        content.endText();

        yPosition -= 13;
    }

    // =====================================================
    // BOLD TEXT
    // =====================================================

    private void writeBoldText(
            PDPageContentStream content,
            String text,
            float fontSize
    ) throws IOException {

        ensureSpace();

        content.beginText();

        content.setFont(
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                ),
                fontSize
        );

        content.newLineAtOffset(
                MARGIN,
                yPosition
        );

        content.showText(clean(text));

        content.endText();

        yPosition -= fontSize + 5;
    }

    // =====================================================
    // NORMAL TEXT
    // =====================================================

    private void writeWrappedText(
            PDPageContentStream content,
            String text,
            float fontSize
    ) throws IOException {

        if (text == null || text.isBlank()) {
            return;
        }

        String cleanText = clean(text);

        int maxCharacters = 105;

        String[] words = cleanText.split("\\s+");

        StringBuilder line = new StringBuilder();

        for (String word : words) {

            if (line.length()
                    + word.length()
                    + 1
                    > maxCharacters) {

                writeLine(
                        content,
                        line.toString(),
                        fontSize
                );

                line = new StringBuilder();
            }

            line.append(word).append(" ");
        }

        if (!line.isEmpty()) {

            writeLine(
                    content,
                    line.toString(),
                    fontSize
            );
        }
    }

    // =====================================================
    // BULLET
    // =====================================================

    private void writeBullet(
            PDPageContentStream content,
            String text
    ) throws IOException {

        if (text == null || text.isBlank()) {
            return;
        }

        writeWrappedText(
                content,
                "• " + text,
                9
        );
    }

    // =====================================================
    // SKILLS
    // =====================================================

    private void writeSkillLine(
            PDPageContentStream content,
            String category,
            java.util.List<String> skills
    ) throws IOException {

        if (skills == null || skills.isEmpty()) {
            return;
        }

        writeWrappedText(
                content,
                category + ": "
                        + String.join(", ", skills),
                9
        );
    }


    // =====================================================
    // writeContact
    // =====================================================

    private void writeContactInfo(
            PDDocument document,
            PDPage page,
            PDPageContentStream content,
            String email,
            String github,
            String linkedin
    ) throws IOException {

        float fontSize = 9;

        PDType1Font font = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA
        );

        String emailText = safe(email);
        String githubText = safe(github);
        String linkedinText = safe(linkedin);

        String separator = " | ";

        // Total text width calculate karo
        float totalWidth = 0;

        if (!emailText.isBlank()) {
            totalWidth += font.getStringWidth(clean(emailText))
                    / 1000 * fontSize;
        }

        if (!githubText.isBlank()) {
            totalWidth += font.getStringWidth(clean(githubText))
                    / 1000 * fontSize;
        }

        if (!linkedinText.isBlank()) {
            totalWidth += font.getStringWidth(clean(linkedinText))
                    / 1000 * fontSize;
        }

        // Separators ki width
        int numberOfLinks = 0;

        if (!emailText.isBlank()) numberOfLinks++;
        if (!githubText.isBlank()) numberOfLinks++;
        if (!linkedinText.isBlank()) numberOfLinks++;

        if (numberOfLinks > 1) {

            totalWidth +=
                    (numberOfLinks - 1)
                            * font.getStringWidth(separator)
                            / 1000
                            * fontSize;
        }

        // Center position calculate
        float x = (PAGE_WIDTH - totalWidth) / 2;

        content.beginText();

        content.setFont(font, fontSize);

        content.newLineAtOffset(
                x,
                yPosition
        );

// =========================
// GITHUB
// =========================

        if (!githubText.isBlank()) {

            content.showText(separator);

            x += font.getStringWidth(separator)
                    / 1000
                    * fontSize;

            float textWidth =
                    font.getStringWidth(clean(githubText))
                            / 1000
                            * fontSize;

            content.showText(clean(githubText));

            PDAnnotationLink link =
                    new PDAnnotationLink();

            // Exact clickable area
            PDRectangle rectangle = new PDRectangle();

            rectangle.setLowerLeftX(x);
            rectangle.setLowerLeftY(yPosition - 2);

            rectangle.setUpperRightX(
                    x + textWidth
            );

            rectangle.setUpperRightY(
                    yPosition + fontSize + 2
            );

            link.setRectangle(rectangle);

            // Click karne par yellow highlight nahi
            link.setHighlightMode(
                    PDAnnotationLink.HIGHLIGHT_MODE_NONE
            );

            PDActionURI action =
                    new PDActionURI();

            action.setURI(
                    githubText.startsWith("http")
                            ? githubText
                            : "https://" + githubText
            );

            link.setAction(action);

            page.getAnnotations().add(link);

            x += textWidth;
        }



        // =========================
        // LINKEDIN
        // =========================

        if (!linkedinText.isBlank()) {

            content.showText(separator);

            x += font.getStringWidth(separator)
                    / 1000
                    * fontSize;

            float textWidth =
                    font.getStringWidth(clean(linkedinText))
                            / 1000
                            * fontSize;

            content.showText(clean(linkedinText));

            PDAnnotationLink link =
                    new PDAnnotationLink();

            link.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_NONE);

            PDRectangle rectangle = new PDRectangle();

            rectangle.setLowerLeftX(x);
            rectangle.setLowerLeftY(yPosition - 2);

            rectangle.setUpperRightX(
                    x + textWidth
            );

            rectangle.setUpperRightY(
                    yPosition + fontSize + 2
            );

            link.setRectangle(rectangle);

            PDActionURI action =
                    new PDActionURI();

            action.setURI(
                    linkedinText.startsWith("http")
                            ? linkedinText
                            : "https://" + linkedinText
            );

            link.setAction(action);

            page.getAnnotations().add(link);
        }

        content.endText();
    }


    // =====================================================
    // writeCentered
    // =====================================================

    private void writeCentered(
            PDPageContentStream content,
            String text,
            float fontSize,
            boolean bold
    ) throws IOException {

        if (text == null || text.isBlank()) {
            return;
        }

        PDType1Font font = new PDType1Font(
                bold
                        ? Standard14Fonts.FontName.HELVETICA_BOLD
                        : Standard14Fonts.FontName.HELVETICA
        );

        float textWidth =
                font.getStringWidth(clean(text))
                        / 1000
                        * fontSize;

        float x =
                (PAGE_WIDTH - textWidth) / 2;

        content.beginText();

        content.setFont(font, fontSize);

        content.newLineAtOffset(
                x,
                yPosition
        );

        content.showText(clean(text));

        content.endText();
    }

    // =====================================================
    // WRITE LINE
    // =====================================================

    private void writeLine(
            PDPageContentStream content,
            String text,
            float fontSize
    ) throws IOException {

        ensureSpace();

        content.beginText();

        content.setFont(
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                ),
                fontSize
        );

        content.newLineAtOffset(
                MARGIN,
                yPosition
        );

        content.showText(clean(text));

        content.endText();

        yPosition -= fontSize + 3;
    }

// =====================================================
// PAGE SPACE
// =====================================================

    private void ensureSpace() {

        if (yPosition < 35) {

            throw new RuntimeException(
                    "Resume content exceeds one A4 page."
            );
        }
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safe(String value) {

        return value == null ? "" : value;
    }

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("•", "-");
    }
}