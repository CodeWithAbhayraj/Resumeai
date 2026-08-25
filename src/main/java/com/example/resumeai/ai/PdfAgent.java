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

    // Reference resume has slightly wider margins
    private static final float MARGIN_LEFT = 42;

    private static final float MARGIN_RIGHT = 42;

    private static final float TOP_MARGIN = 30;

    private static final float BOTTOM_MARGIN = 28;

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
                        18,
                        FONT_BOLD
                );

                // Small gap after name
                yPosition -= 2;


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

                yPosition -= 3;


                // =================================================
                // SUMMARY
                // =================================================

                if (hasText(resume.getProfessionalSummary())) {

                    writeSectionTitle(
                            content,
                            "Summary"
                    );

                    writeWrappedText(
                            content,
                            resume.getProfessionalSummary(),
                            9
                    );

                    yPosition -= 2;
                }


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

                        // Small gap between projects
                        yPosition -= 1.5f;
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

                        yPosition -= 1.5f;
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

                        yPosition -= 1.5f;
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

                    yPosition -= 1;
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
            // SAVE PDF
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

        if (!hasText(title)) {
            return;
        }

        ensureSpace();

        // Small top spacing
        yPosition -= 1;


        // =================================================
        // TITLE
        // =================================================

        content.beginText();

        content.setFont(
                FONT_BOLD,
                10.5f
        );

        content.setNonStrokingColor(
                0,
                0,
                0
        );

        content.newLineAtOffset(
                MARGIN_LEFT,
                yPosition
        );

        content.showText(
                clean(title)
        );

        content.endText();


        // =================================================
        // HORIZONTAL LINE
        // =================================================

        float titleWidth =
                getTextWidth(
                        title,
                        FONT_BOLD,
                        10.5f
                );

        float lineY =
                yPosition - 2.5f;

        content.setLineWidth(0.5f);

        content.moveTo(
                MARGIN_LEFT + titleWidth + 6,
                lineY
        );

        content.lineTo(
                PAGE_WIDTH - MARGIN_RIGHT,
                lineY
        );

        content.stroke();


        // Compact spacing after heading
        yPosition -= 12;
    }


    // =====================================================
    // CENTERED NAME
    // =====================================================

    private void writeCentered(
            PDPageContentStream content,
            String text,
            float fontSize,
            PDType1Font font
    ) throws IOException {

        if (!hasText(text)) {
            return;
        }

        String cleanText =
                clean(text);

        float textWidth =
                getTextWidth(
                        cleanText,
                        font,
                        fontSize
                );

        float x =
                (PAGE_WIDTH - textWidth) / 2;


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
                yPosition
        );

        content.showText(
                cleanText
        );

        content.endText();


        yPosition -=
                fontSize + 1;
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

        float fontSize = 8.5f;

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

        if (hasText(emailText)) {
            count++;
        }

        if (hasText(linkedinText)) {
            count++;
        }

        if (hasText(githubText)) {
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

        if (hasText(emailText)) {

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

        if (hasText(linkedinText)) {

            if (hasText(emailText)) {

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

        if (hasText(githubText)) {

            if (hasText(emailText)
                    || hasText(linkedinText)) {

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


        yPosition -= 10;
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

        content.setNonStrokingColor(
                new Color(0, 70, 180)
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                clean(text)
        );

        content.endText();


        // Reset black
        content.setNonStrokingColor(
                0,
                0,
                0
        );


        // =================================================
        // CLICKABLE LINK
        // =================================================

        if (hasText(url)) {

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

            link.setAction(action);

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


        float titleFontSize = 10;

        float techFontSize = 9;


        float titleWidth =
                getTextWidth(
                        title,
                        FONT_BOLD,
                        titleFontSize
                );

        float techWidth =
                getTextWidth(
                        technologies,
                        FONT_ITALIC,
                        techFontSize
                );


        // =================================================
        // SAME LINE
        // =================================================

        if (hasText(technologies)
                && titleWidth
                + techWidth
                + 20
                <= CONTENT_WIDTH) {

            // PROJECT TITLE
            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    titleFontSize
            );


            // TECHNOLOGIES RIGHT
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
                    techFontSize
            );

            yPosition -= 12;

        } else {

            // TITLE
            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    titleFontSize
            );

            yPosition -= 11;


            // TECHNOLOGIES
            if (hasText(technologies)) {

                writeWrappedTextWithFont(
                        content,
                        technologies,
                        MARGIN_LEFT,
                        techFontSize,
                        FONT_ITALIC
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

        float institutionFontSize = 9.5f;

        String institution =
                safe(education.getInstitution());


        String startDate =
                safe(education.getStartDate());

        String endDate =
                safe(education.getEndDate());


        String dates = "";

        if (hasText(startDate)
                || hasText(endDate)) {

            dates =
                    startDate
                            + " - "
                            + endDate;
        }


        float dateWidth =
                getTextWidth(
                        dates,
                        FONT_ITALIC,
                        8.5f
                );


        // =================================================
        // INSTITUTION
        // =================================================

        drawNormalText(
                content,
                institution,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                institutionFontSize
        );


        // =================================================
        // DATE
        // =================================================

        if (hasText(dates)) {

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
                    8.5f
            );
        }


        yPosition -= 11;


        // =================================================
        // DEGREE
        // =================================================

        writeWrappedText(
                content,
                safe(education.getDegree()),
                8.5f
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


        if (hasText(company)) {

            heading +=
                    " - " + company;
        }


        drawNormalText(
                content,
                heading,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                9.5f
        );

        yPosition -= 11;


        if (hasText(experience.getDuration())) {

            writeWrappedText(
                    content,
                    experience.getDuration(),
                    8.5f
            );
        }


        if (hasText(experience.getDescription())) {

            writeBullet(
                    content,
                    experience.getDescription()
            );
        }
    }


    // =====================================================
    // TECHNICAL SKILLS
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


        float fontSize = 8.5f;


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
        // FIXED LABEL COLUMN
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
                        + 10;


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

        if (!hasText(text)) {
            return;
        }

        ensureSpace();

        float fontSize = 8.7f;


        // =================================================
        // BULLET SYMBOL
        // =================================================

        drawNormalText(
                content,
                "•",
                MARGIN_LEFT + 6,
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
                MARGIN_LEFT + 17,
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

        writeWrappedTextWithFont(
                content,
                text,
                x,
                fontSize,
                FONT_REGULAR
        );
    }


    // =====================================================
    // WRAPPED TEXT WITH FONT
    // =====================================================

    private void writeWrappedTextWithFont(
            PDPageContentStream content,
            String text,
            float x,
            float fontSize,
            PDType1Font font
    ) throws IOException {

        if (!hasText(text)) {
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
                            font,
                            fontSize
                    );


            if (testWidth
                    > availableWidth
                    && !line.isEmpty()) {

                writeLineAtX(
                        content,
                        line.toString(),
                        x,
                        fontSize,
                        font
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
                    fontSize,
                    font
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
            float fontSize,
            PDType1Font font
    ) throws IOException {

        ensureSpace();

        drawNormalText(
                content,
                text,
                x,
                yPosition,
                font,
                fontSize
        );


        // Tight line spacing like reference resume
        yPosition -=
                fontSize + 1.8f;
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

        if (!hasText(text)) {
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

        if (!hasText(text)) {
            return 0;
        }


        try {

            return font.getStringWidth(
                    clean(text)
            ) / 1000f * fontSize;

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
    // SAFE
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    // =====================================================
    // HAS TEXT
    // =====================================================

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
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