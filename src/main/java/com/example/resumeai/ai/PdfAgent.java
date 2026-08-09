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

import org.apache.pdfbox.pdmodel.common.PDRectangle;
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
        float x = MARGIN;

        String[] labels = {
                safe(email),
                safe(github),
                safe(linkedin)
        };

        String[] urls = {
                "mailto:" + safe(email),
                safe(github).startsWith("http")
                        ? safe(github)
                        : "https://" + safe(github),
                safe(linkedin).startsWith("http")
                        ? safe(linkedin)
                        : "https://" + safe(linkedin)
        };

        PDType1Font font = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA
        );

        content.beginText();

        content.setFont(font, fontSize);

        content.newLineAtOffset(x, yPosition);

        for (int i = 0; i < labels.length; i++) {

            String text = labels[i];

            if (text.isBlank()) {
                continue;
            }

            float textWidth =
                    font.getStringWidth(clean(text))
                            / 1000
                            * fontSize;

            // Draw text
            content.showText(clean(text));

            // Create clickable area
            PDAnnotationLink link =
                    new PDAnnotationLink();

            link.setRectangle(
                    new PDRectangle(
                            x,
                            yPosition - 3,
                            textWidth,
                            fontSize + 5
                    )
            );

            PDActionURI action =
                    new PDActionURI();

            action.setURI(urls[i]);

            link.setAction(action);

            page.getAnnotations().add(link);

            x += textWidth;

            // Separator
            if (i < labels.length - 1) {

                String separator = " | ";

                content.showText(separator);

                x += font.getStringWidth(separator)
                        / 1000
                        * fontSize;
            }
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