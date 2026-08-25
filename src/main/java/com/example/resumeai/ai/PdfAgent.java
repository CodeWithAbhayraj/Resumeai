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
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class PdfAgent {

    // =====================================================
    // PAGE SETTINGS
    // =====================================================

    private static final float PAGE_WIDTH =
            PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT =
            PDRectangle.A4.getHeight();

    private static final float MARGIN_LEFT = 28;

    private static final float MARGIN_RIGHT = 28;

    private static final float TOP_MARGIN = 30;

    private static final float BOTTOM_MARGIN = 30;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    private float yPosition;


    // =====================================================
    // FONTS
    // =====================================================

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_ROMAN
            );

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_BOLD
            );

    private static final PDType1Font FONT_ITALIC =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_ITALIC
            );

    private static final PDType1Font FONT_BOLD_ITALIC =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_BOLD_ITALIC
            );


    // =====================================================
    // MAIN PDF GENERATOR
    // =====================================================

    public byte[] generateResumePdf(
            ImprovedResumeDTO resume
    ) {

        try (PDDocument document = new PDDocument()) {

            PDPage page =
                    new PDPage(PDRectangle.A4);

            document.addPage(page);

            yPosition =
                    PAGE_HEIGHT - TOP_MARGIN;


            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                // =================================================
                // NAME
                // =================================================

                writeCentered(
                        content,
                        safe(resume.getFullName()),
                        20,
                        FONT_BOLD
                );

                yPosition -= 10;


                // =================================================
                // CONTACT INFORMATION
                // =================================================

                writeContactInfo(
                        document,
                        page,
                        content,
                        resume.getEmail(),
                        resume.getLinkedin(),
                        resume.getGithub()
                );

                yPosition -= 8;


                // =================================================
                // SUMMARY
                // =================================================

                writeSectionTitle(
                        content,
                        "Summary"
                );

                writeWrappedText(
                        content,
                        resume.getProfessionalSummary(),
                        10
                );

                yPosition -= 3;


                // =================================================
                // PROJECTS
                // =================================================

                if (resume.getProjects() != null
                        && !resume.getProjects().isEmpty()) {

                    writeSectionTitle(
                            content,
                            "Projects"
                    );

                    for (ProjectDTO project :
                            resume.getProjects()) {

                        writeProjectHeader(
                                content,
                                project
                        );

                        if (project.getHighlights() != null) {

                            for (String highlight :
                                    project.getHighlights()) {

                                writeBullet(
                                        content,
                                        highlight
                                );
                            }
                        }

                        yPosition -= 3;
                    }
                }


                // =================================================
                // EDUCATION
                // =================================================

                if (resume.getEducation() != null
                        && !resume.getEducation().isEmpty()) {

                    writeSectionTitle(
                            content,
                            "Education"
                    );

                    for (EducationDTO education :
                            resume.getEducation()) {

                        writeEducation(
                                content,
                                education
                        );

                        yPosition -= 3;
                    }
                }


                // =================================================
                // EXPERIENCE
                // =================================================

                if (resume.getExperience() != null
                        && !resume.getExperience().isEmpty()) {

                    writeSectionTitle(
                            content,
                            "Experience"
                    );

                    for (ExperienceDTO experience :
                            resume.getExperience()) {

                        writeExperience(
                                content,
                                experience
                        );

                        yPosition -= 3;
                    }
                }


                // =================================================
                // CERTIFICATIONS
                // =================================================

                if (resume.getCertifications() != null
                        && !resume.getCertifications().isEmpty()) {

                    writeSectionTitle(
                            content,
                            "Certifications"
                    );

                    for (String certification :
                            resume.getCertifications()) {

                        writeBullet(
                                content,
                                certification
                        );
                    }
                }


                // =================================================
                // TECHNICAL SKILLS
                // =================================================

                SkillsDTO skills =
                        resume.getSkills();

                if (skills != null) {

                    writeSectionTitle(
                            content,
                            "Technical Skills"
                    );

                    writeSkillLine(
                            content,
                            "Languages:",
                            skills.getLanguages()
                    );

                    writeSkillLine(
                            content,
                            "Frameworks:",
                            skills.getFrameworks()
                    );

                    writeSkillLine(
                            content,
                            "Databases:",
                            skills.getDatabases()
                    );

                    writeSkillLine(
                            content,
                            "DevOps & Tools:",
                            skills.getDevopsAndTools()
                    );

                    writeSkillLine(
                            content,
                            "Concepts:",
                            skills.getConcepts()
                    );
                }
            }


            // =====================================================
            // SAVE
            // =====================================================

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

        ensureSpace();

        yPosition -= 2;


        // ---------------------------------------------
        // TITLE
        // ---------------------------------------------

        content.beginText();

        content.setFont(
                FONT_BOLD,
                11
        );

        content.newLineAtOffset(
                MARGIN_LEFT,
                yPosition
        );

        content.showText(
                clean(title)
        );

        content.endText();


        // ---------------------------------------------
        // HORIZONTAL LINE
        // ---------------------------------------------

        float titleWidth =
                FONT_BOLD.getStringWidth(
                        clean(title)
                ) / 1000 * 11;

        float lineY =
                yPosition - 2;

        content.moveTo(
                MARGIN_LEFT + titleWidth + 6,
                lineY
        );

        content.lineTo(
                PAGE_WIDTH - MARGIN_RIGHT,
                lineY
        );

        content.stroke();


        yPosition -= 13;
    }


    // =====================================================
    // CENTERED TEXT
    // =====================================================

    private void writeCentered(
            PDPageContentStream content,
            String text,
            float fontSize,
            PDType1Font font
    ) throws IOException {

        if (text == null || text.isBlank()) {
            return;
        }

        String cleanText =
                clean(text);

        float textWidth =
                font.getStringWidth(cleanText)
                        / 1000
                        * fontSize;

        float x =
                (PAGE_WIDTH - textWidth) / 2;


        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.newLineAtOffset(
                x,
                yPosition
        );

        content.showText(
                cleanText
        );

        content.endText();


        yPosition -= fontSize + 2;
    }


    // =====================================================
    // CONTACT INFORMATION
    // =====================================================

    private void writeContactInfo(
            PDDocument document,
            PDPage page,
            PDPageContentStream content,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        float fontSize = 9;

        String emailText =
                safe(email);

        String linkedinText =
                safe(linkedin);

        String githubText =
                safe(github);


        String separator =
                "  —  ";


        float emailWidth =
                getTextWidth(
                        emailText,
                        FONT_REGULAR,
                        fontSize
                );

        float linkedinWidth =
                getTextWidth(
                        linkedinText,
                        FONT_REGULAR,
                        fontSize
                );

        float githubWidth =
                getTextWidth(
                        githubText,
                        FONT_REGULAR,
                        fontSize
                );

        float separatorWidth =
                getTextWidth(
                        separator,
                        FONT_REGULAR,
                        fontSize
                );


        int count = 0;

        if (!emailText.isBlank()) {
            count++;
        }

        if (!linkedinText.isBlank()) {
            count++;
        }

        if (!githubText.isBlank()) {
            count++;
        }


        float totalWidth =
                emailWidth
                        + linkedinWidth
                        + githubWidth
                        + Math.max(0, count - 1)
                        * separatorWidth;


        float x =
                (PAGE_WIDTH - totalWidth) / 2;


        // =================================================
        // EMAIL
        // =================================================

        if (!emailText.isBlank()) {

            drawContactLink(
                    document,
                    page,
                    content,
                    emailText,
                    x,
                    yPosition,
                    emailWidth,
                    fontSize,
                    null
            );

            x += emailWidth;
        }


        // =================================================
        // LINKEDIN
        // =================================================

        if (!linkedinText.isBlank()) {

            if (!emailText.isBlank()) {

                drawNormalText(
                        content,
                        separator,
                        x,
                        yPosition,
                        FONT_REGULAR,
                        fontSize
                );

                x += separatorWidth;
            }

            drawContactLink(
                    document,
                    page,
                    content,
                    linkedinText,
                    x,
                    yPosition,
                    linkedinWidth,
                    fontSize,
                    linkedinText
            );

            x += linkedinWidth;
        }


        // =================================================
        // GITHUB
        // =================================================

        if (!githubText.isBlank()) {

            if (!emailText.isBlank()
                    || !linkedinText.isBlank()) {

                drawNormalText(
                        content,
                        separator,
                        x,
                        yPosition,
                        FONT_REGULAR,
                        fontSize
                );

                x += separatorWidth;
            }

            drawContactLink(
                    document,
                    page,
                    content,
                    githubText,
                    x,
                    yPosition,
                    githubWidth,
                    fontSize,
                    githubText
            );
        }


        yPosition -= 11;
    }


    // =====================================================
    // CONTACT LINK
    // =====================================================

    private void drawContactLink(
            PDDocument document,
            PDPage page,
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float width,
            float fontSize,
            String url
    ) throws IOException {

        content.beginText();

        content.setFont(
                FONT_REGULAR,
                fontSize
        );

        // Blue link
        content.setNonStrokingColor(new Color(0, 70, 180));

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                clean(text)
        );

        content.endText();


        // Reset to black
        content.setNonStrokingColor(
                0,
                0,
                0
        );


        // =================================================
        // PDF CLICKABLE LINK
        // =================================================

        if (url != null
                && !url.isBlank()) {

            PDAnnotationLink link =
                    new PDAnnotationLink();

            PDRectangle rectangle =
                    new PDRectangle();

            rectangle.setLowerLeftX(x);

            rectangle.setLowerLeftY(
                    y - 2
            );

            rectangle.setUpperRightX(
                    x + width
            );

            rectangle.setUpperRightY(
                    y + fontSize + 2
            );

            link.setRectangle(
                    rectangle
            );

            link.setHighlightMode(
                    PDAnnotationLink
                            .HIGHLIGHT_MODE_NONE
            );

            PDActionURI action =
                    new PDActionURI();

            action.setURI(
                    url.startsWith("http")
                            ? url
                            : "https://" + url
            );

            link.setAction(
                    action
            );

            page.getAnnotations()
                    .add(link);
        }
    }


    // =====================================================
    // PROJECT HEADER
    // =====================================================

    private void writeProjectHeader(
            PDPageContentStream content,
            ProjectDTO project
    ) throws IOException {

        ensureSpace();

        String title =
                safe(project.getTitle());

        String technologies = "";

        if (project.getTechnologies() != null
                && !project.getTechnologies().isEmpty()) {

            technologies =
                    String.join(
                            " | ",
                            project.getTechnologies()
                    );
        }


        float fontSize = 10;


        // =================================================
        // TITLE WIDTH
        // =================================================

        float titleWidth =
                getTextWidth(
                        title,
                        FONT_BOLD,
                        fontSize
                );


        float techWidth =
                getTextWidth(
                        technologies,
                        FONT_ITALIC,
                        fontSize
                );


        // =================================================
        // IF TECHNOLOGIES FIT ON SAME LINE
        // =================================================

        if (!technologies.isBlank()
                && titleWidth
                + techWidth
                + 25
                <= CONTENT_WIDTH) {

            // LEFT TITLE
            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    fontSize
            );


            // RIGHT TECHNOLOGIES
            float techX =
                    PAGE_WIDTH
                            - MARGIN_RIGHT
                            - techWidth;

            drawNormalText(
                    content,
                    technologies,
                    techX,
                    yPosition,
                    FONT_ITALIC,
                    fontSize
            );

            yPosition -= 13;

        } else {

            // ---------------------------------------------
            // TITLE
            // ---------------------------------------------

            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    fontSize
            );

            yPosition -= 12;


            // ---------------------------------------------
            // TECHNOLOGIES
            // ---------------------------------------------

            if (!technologies.isBlank()) {

                writeWrappedText(
                        content,
                        technologies,
                        9
                );
            }
        }
    }


    // =====================================================
    // EDUCATION
    // =====================================================

    private void writeEducation(
            PDPageContentStream content,
            EducationDTO education
    ) throws IOException {

        ensureSpace();

        float fontSize = 10;


        String institution =
                safe(education.getInstitution());


        String dates =
                safe(education.getStartDate())
                        + " - "
                        + safe(education.getEndDate());


        float dateWidth =
                getTextWidth(
                        dates,
                        FONT_ITALIC,
                        9
                );


        // =================================================
        // INSTITUTION LEFT
        // =================================================

        drawNormalText(
                content,
                institution,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                fontSize
        );


        // =================================================
        // DATE RIGHT
        // =================================================

        if (!dates.equals(" - ")) {

            float dateX =
                    PAGE_WIDTH
                            - MARGIN_RIGHT
                            - dateWidth;

            drawNormalText(
                    content,
                    dates,
                    dateX,
                    yPosition,
                    FONT_ITALIC,
                    9
            );
        }


        yPosition -= 12;


        // =================================================
        // DEGREE
        // =================================================

        writeWrappedText(
                content,
                safe(education.getDegree()),
                9
        );
    }


    // =====================================================
    // EXPERIENCE
    // =====================================================

    private void writeExperience(
            PDPageContentStream content,
            ExperienceDTO experience
    ) throws IOException {

        ensureSpace();

        String role =
                safe(experience.getRole());

        String company =
                safe(experience.getCompany());


        String heading =
                role;


        if (!company.isBlank()) {

            heading +=
                    " - " + company;
        }


        // ROLE + COMPANY
        drawNormalText(
                content,
                heading,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                10
        );

        yPosition -= 12;


        // DURATION

        if (experience.getDuration() != null
                && !experience.getDuration().isBlank()) {

            writeWrappedText(
                    content,
                    experience.getDuration(),
                    9
            );
        }


        // DESCRIPTION

        if (experience.getDescription() != null
                && !experience.getDescription().isBlank()) {

            writeBullet(
                    content,
                    experience.getDescription()
            );
        }
    }


    // =====================================================
    // SKILLS
    // =====================================================

    private void writeSkillLine(
            PDPageContentStream content,
            String category,
            List<String> skills
    ) throws IOException {

        if (skills == null
                || skills.isEmpty()) {

            return;
        }


        String skillText =
                String.join(
                        ", ",
                        skills
                );


        float fontSize = 9;


        // =================================================
        // CATEGORY
        // =================================================

        drawNormalText(
                content,
                category,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                fontSize
        );


        // =================================================
        // CATEGORY FIXED WIDTH
        // =================================================

        float categoryWidth =
                getTextWidth(
                        "DevOps & Tools:",
                        FONT_BOLD,
                        fontSize
                );


        float skillX =
                MARGIN_LEFT
                        + categoryWidth
                        + 12;


        // =================================================
        // SKILLS
        // =================================================

        writeWrappedTextAtX(
                content,
                skillText,
                skillX,
                fontSize
        );
    }


    // =====================================================
    // BULLET
    // =====================================================

    private void writeBullet(
            PDPageContentStream content,
            String text
    ) throws IOException {

        if (text == null
                || text.isBlank()) {

            return;
        }


        ensureSpace();


        float fontSize = 9;


        // =================================================
        // BULLET
        // =================================================

        drawNormalText(
                content,
                "•",
                MARGIN_LEFT + 8,
                yPosition,
                FONT_REGULAR,
                fontSize
        );


        // =================================================
        // BULLET TEXT
        // =================================================

        writeWrappedTextAtX(
                content,
                text,
                MARGIN_LEFT + 20,
                fontSize
        );
    }


    // =====================================================
    // WRAPPED TEXT
    // =====================================================

    private void writeWrappedText(
            PDPageContentStream content,
            String text,
            float fontSize
    ) throws IOException {

        writeWrappedTextAtX(
                content,
                text,
                MARGIN_LEFT,
                fontSize
        );
    }


    // =====================================================
    // WRAPPED TEXT AT X
    // =====================================================

    private void writeWrappedTextAtX(
            PDPageContentStream content,
            String text,
            float x,
            float fontSize
    ) throws IOException {

        if (text == null
                || text.isBlank()) {

            return;
        }


        String cleanText =
                clean(text);


        String[] words =
                cleanText.split("\\s+");


        StringBuilder line =
                new StringBuilder();


        float availableWidth =
                PAGE_WIDTH
                        - MARGIN_RIGHT
                        - x;


        for (String word : words) {

            String testLine;

            if (line.isEmpty()) {

                testLine = word;

            } else {

                testLine =
                        line + " " + word;
            }


            float testWidth =
                    getTextWidth(
                            testLine,
                            FONT_REGULAR,
                            fontSize
                    );


            if (testWidth
                    > availableWidth
                    && !line.isEmpty()) {

                writeLineAtX(
                        content,
                        line.toString(),
                        x,
                        fontSize
                );

                line =
                        new StringBuilder(word);

            } else {

                line =
                        new StringBuilder(
                                testLine
                        );
            }
        }


        if (!line.isEmpty()) {

            writeLineAtX(
                    content,
                    line.toString(),
                    x,
                    fontSize
            );
        }
    }


    // =====================================================
    // WRITE LINE
    // =====================================================

    private void writeLineAtX(
            PDPageContentStream content,
            String text,
            float x,
            float fontSize
    ) throws IOException {

        ensureSpace();


        drawNormalText(
                content,
                text,
                x,
                yPosition,
                FONT_REGULAR,
                fontSize
        );


        yPosition -=
                fontSize + 2.5f;
    }


    // =====================================================
    // DRAW NORMAL TEXT
    // =====================================================

    private void drawNormalText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            PDType1Font font,
            float fontSize
    ) throws IOException {

        if (text == null
                || text.isBlank()) {

            return;
        }


        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.setNonStrokingColor(
                0,
                0,
                0
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                clean(text)
        );

        content.endText();
    }


    // =====================================================
    // TEXT WIDTH
    // =====================================================

    private float getTextWidth(
            String text,
            PDType1Font font,
            float fontSize
    ) {

        if (text == null
                || text.isBlank()) {

            return 0;
        }


        try {

            return font.getStringWidth(
                    clean(text)
            ) / 1000 * fontSize;

        } catch (IOException e) {

            return 0;
        }
    }


    // =====================================================
    // PAGE SPACE
    // =====================================================

    private void ensureSpace() {

        if (yPosition < BOTTOM_MARGIN) {

            throw new RuntimeException(
                    "Resume content exceeds one A4 page."
            );
        }
    }


    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    // =====================================================
    // CLEAN TEXT
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null) {
            return "";
        }


        return value
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"");
    }
}