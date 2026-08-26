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

    // =========================================================
    // PAGE
    // =========================================================

    private static final PDRectangle PAGE_SIZE = PDRectangle.A4;

    private static final float PAGE_WIDTH = PAGE_SIZE.getWidth();
    private static final float PAGE_HEIGHT = PAGE_SIZE.getHeight();

    /*
     * Reference resume:
     * - balanced left/right margins
     * - compact but readable
     * - single A4 page
     */
    private static final float LEFT_MARGIN = 52f;
    private static final float RIGHT_MARGIN = 52f;

    private static final float TOP_MARGIN = 38f;
    private static final float BOTTOM_MARGIN = 30f;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;


    // =========================================================
    // FONTS
    // =========================================================

    private static final PDType1Font REGULAR =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_ROMAN
            );

    private static final PDType1Font BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_BOLD
            );

    private static final PDType1Font ITALIC =
            new PDType1Font(
                    Standard14Fonts.FontName.TIMES_ITALIC
            );


    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float NAME_SIZE = 18f;

    private static final float CONTACT_SIZE = 8.5f;

    private static final float SECTION_SIZE = 10.5f;

    private static final float SUMMARY_SIZE = 9f;

    private static final float PROJECT_TITLE_SIZE = 10f;

    private static final float PROJECT_TECH_SIZE = 8.8f;

    private static final float BULLET_SIZE = 8.7f;

    private static final float EDUCATION_INSTITUTION_SIZE = 9.5f;

    private static final float EDUCATION_DETAILS_SIZE = 8.7f;

    private static final float DATE_SIZE = 8.3f;

    private static final float CERTIFICATION_SIZE = 8.7f;

    private static final float SKILLS_SIZE = 8.5f;


    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BLACK =
            Color.BLACK;

    private static final Color LINK_BLUE =
            new Color(0, 70, 180);


    // =========================================================
    // INTERNAL PAGE CONTEXT
    // =========================================================

    /*
     * IMPORTANT:
     *
     * Do not keep yPosition as a class-level field.
     *
     * PdfAgent is a Spring singleton. A shared yPosition can
     * cause problems when multiple users generate PDFs at once.
     */
    private static class PageContext {

        private float y;

        PageContext(float y) {
            this.y = y;
        }
    }


    // =========================================================
    // MAIN PDF GENERATOR
    // =========================================================

    public byte[] generateResumePdf(
            ImprovedResumeDTO resume
    ) {

        if (resume == null) {
            throw new IllegalArgumentException(
                    "Resume data cannot be null."
            );
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page =
                    new PDPage(PAGE_SIZE);

            document.addPage(page);

            PageContext ctx =
                    new PageContext(
                            PAGE_HEIGHT - TOP_MARGIN
                    );

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                // =================================================
                // 1. NAME
                // =================================================

                writeName(
                        content,
                        ctx,
                        resume.getFullName()
                );


                // =================================================
                // 2. CONTACT
                // =================================================

                writeContactInfo(
                        document,
                        page,
                        content,
                        ctx,
                        resume.getEmail(),
                        resume.getLinkedin(),
                        resume.getGithub()
                );


                // =================================================
                // 3. SUMMARY
                // =================================================

                if (hasText(
                        resume.getProfessionalSummary()
                )) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Summary"
                    );

                    writeWrappedText(
                            content,
                            ctx,
                            resume.getProfessionalSummary(),
                            LEFT_MARGIN,
                            SUMMARY_SIZE,
                            REGULAR,
                            1.6f
                    );

                    ctx.y -= 2f;
                }


                // =================================================
                // 4. PROJECTS
                // =================================================

                if (resume.getProjects() != null
                        && !resume.getProjects().isEmpty()) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Projects"
                    );

                    for (ProjectDTO project :
                            resume.getProjects()) {

                        writeProject(
                                content,
                                ctx,
                                project
                        );

                        ctx.y -= 1.5f;
                    }
                }


                // =================================================
                // 5. EDUCATION
                // =================================================

                if (resume.getEducation() != null
                        && !resume.getEducation().isEmpty()) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Education"
                    );

                    for (EducationDTO education :
                            resume.getEducation()) {

                        writeEducation(
                                content,
                                ctx,
                                education
                        );

                        ctx.y -= 1.5f;
                    }
                }


                // =================================================
                // 6. EXPERIENCE
                // =================================================

                if (resume.getExperience() != null
                        && !resume.getExperience().isEmpty()) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Experience"
                    );

                    for (ExperienceDTO experience :
                            resume.getExperience()) {

                        writeExperience(
                                content,
                                ctx,
                                experience
                        );

                        ctx.y -= 1.5f;
                    }
                }


                // =================================================
                // 7. CERTIFICATIONS
                // =================================================

                if (resume.getCertifications() != null
                        && !resume.getCertifications().isEmpty()) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Certifications"
                    );

                    for (String certification :
                            resume.getCertifications()) {

                        if (hasText(certification)) {

                            writeBullet(
                                    content,
                                    ctx,
                                    certification,
                                    CERTIFICATION_SIZE
                            );
                        }
                    }

                    ctx.y -= 1f;
                }


                // =================================================
                // 8. TECHNICAL SKILLS
                // =================================================

                SkillsDTO skills =
                        resume.getSkills();

                if (skills != null) {

                    writeSectionTitle(
                            content,
                            ctx,
                            "Technical Skills"
                    );

                    writeSkillLine(
                            content,
                            ctx,
                            "Languages:",
                            skills.getLanguages()
                    );

                    writeSkillLine(
                            content,
                            ctx,
                            "Frameworks:",
                            skills.getFrameworks()
                    );

                    writeSkillLine(
                            content,
                            ctx,
                            "Databases:",
                            skills.getDatabases()
                    );

                    writeSkillLine(
                            content,
                            ctx,
                            "DevOps & Tools:",
                            skills.getDevopsAndTools()
                    );

                    writeSkillLine(
                            content,
                            ctx,
                            "Concepts:",
                            skills.getConcepts()
                    );
                }


                // =================================================
                // FINAL PAGE CHECK
                // =================================================

                if (ctx.y < BOTTOM_MARGIN) {

                    throw new RuntimeException(
                            "Resume content exceeds one A4 page. " +
                                    "Please reduce resume content."
                    );
                }
            }


            // =====================================================
            // SAVE
            // =====================================================

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            document.save(output);

            return output.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to generate resume PDF.",
                    e
            );
        }
    }


    // =========================================================
    // NAME
    // =========================================================

    private void writeName(
            PDPageContentStream content,
            PageContext ctx,
            String name
    ) throws IOException {

        if (!hasText(name)) {
            return;
        }

        String text =
                clean(name);

        float width =
                getTextWidth(
                        text,
                        BOLD,
                        NAME_SIZE
                );

        float x =
                (PAGE_WIDTH - width) / 2f;

        drawText(
                content,
                text,
                x,
                ctx.y,
                BOLD,
                NAME_SIZE,
                BLACK
        );

        ctx.y -= 19f;
    }


    // =========================================================
    // CONTACT INFORMATION
    // =========================================================

    private void writeContactInfo(
            PDDocument document,
            PDPage page,
            PDPageContentStream content,
            PageContext ctx,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        String emailText =
                clean(safe(email));

        String linkedinText =
                clean(safe(linkedin));

        String githubText =
                clean(safe(github));

        String separator =
                "   —   ";


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

        if (count == 0) {
            return;
        }


        float emailWidth =
                getTextWidth(
                        emailText,
                        REGULAR,
                        CONTACT_SIZE
                );

        float linkedinWidth =
                getTextWidth(
                        linkedinText,
                        REGULAR,
                        CONTACT_SIZE
                );

        float githubWidth =
                getTextWidth(
                        githubText,
                        REGULAR,
                        CONTACT_SIZE
                );

        float separatorWidth =
                getTextWidth(
                        separator,
                        REGULAR,
                        CONTACT_SIZE
                );


        float totalWidth =
                emailWidth
                        + linkedinWidth
                        + githubWidth
                        + ((count - 1) * separatorWidth);


        float x =
                (PAGE_WIDTH - totalWidth) / 2f;


        // =====================================================
        // EMAIL
        // =====================================================

        if (hasText(emailText)) {

            drawContactLink(
                    document,
                    page,
                    content,
                    emailText,
                    x,
                    ctx.y,
                    emailWidth,
                    CONTACT_SIZE,
                    null
            );

            x += emailWidth;
        }


        // =====================================================
        // LINKEDIN
        // =====================================================

        if (hasText(linkedinText)) {

            if (hasText(emailText)) {

                drawText(
                        content,
                        separator,
                        x,
                        ctx.y,
                        REGULAR,
                        CONTACT_SIZE,
                        BLACK
                );

                x += separatorWidth;
            }

            drawContactLink(
                    document,
                    page,
                    content,
                    linkedinText,
                    x,
                    ctx.y,
                    linkedinWidth,
                    CONTACT_SIZE,
                    linkedinText
            );

            x += linkedinWidth;
        }


        // =====================================================
        // GITHUB
        // =====================================================

        if (hasText(githubText)) {

            if (hasText(emailText)
                    || hasText(linkedinText)) {

                drawText(
                        content,
                        separator,
                        x,
                        ctx.y,
                        REGULAR,
                        CONTACT_SIZE,
                        BLACK
                );

                x += separatorWidth;
            }

            drawContactLink(
                    document,
                    page,
                    content,
                    githubText,
                    x,
                    ctx.y,
                    githubWidth,
                    CONTACT_SIZE,
                    githubText
            );
        }


        ctx.y -= 13f;
    }


    // =========================================================
    // CLICKABLE CONTACT LINK
    // =========================================================

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

        drawText(
                content,
                text,
                x,
                y,
                REGULAR,
                fontSize,
                LINK_BLUE
        );


        if (!hasText(url)) {
            return;
        }


        PDAnnotationLink link =
                new PDAnnotationLink();

        PDRectangle rectangle =
                new PDRectangle();

        rectangle.setLowerLeftX(x);

        rectangle.setLowerLeftY(
                y - 2f
        );

        rectangle.setUpperRightX(
                x + width
        );

        rectangle.setUpperRightY(
                y + fontSize + 2f
        );

        link.setRectangle(
                rectangle
        );

        link.setHighlightMode(
                PDAnnotationLink.HIGHLIGHT_MODE_NONE
        );


        PDActionURI action =
                new PDActionURI();

        action.setURI(
                url.startsWith("http://")
                        || url.startsWith("https://")
                        ? url
                        : "https://" + url
        );

        link.setAction(action);

        page.getAnnotations().add(link);
    }


    // =========================================================
    // SECTION TITLE
    // =========================================================

    private void writeSectionTitle(
            PDPageContentStream content,
            PageContext ctx,
            String title
    ) throws IOException {

        if (!hasText(title)) {
            return;
        }


        /*
         * Reference layout:
         *
         * Summary ------------------------------
         *
         * Text...
         */

        ctx.y -= 1f;

        ensureSpace(
                ctx,
                15f
        );


        float titleWidth =
                getTextWidth(
                        title,
                        BOLD,
                        SECTION_SIZE
                );


        drawText(
                content,
                title,
                LEFT_MARGIN,
                ctx.y,
                BOLD,
                SECTION_SIZE,
                BLACK
        );


        float lineY =
                ctx.y - 2.5f;


        content.setStrokingColor(
                BLACK
        );

        content.setLineWidth(
                0.45f
        );

        content.moveTo(
                LEFT_MARGIN + titleWidth + 6f,
                lineY
        );

        content.lineTo(
                PAGE_WIDTH - RIGHT_MARGIN,
                lineY
        );

        content.stroke();


        /*
         * Space after heading.
         */
        ctx.y -= 12f;
    }


    // =========================================================
    // PROJECT
    // =========================================================

    private void writeProject(
            PDPageContentStream content,
            PageContext ctx,
            ProjectDTO project
    ) throws IOException {

        if (project == null) {
            return;
        }


        ensureSpace(
                ctx,
                12f
        );


        String title =
                clean(
                        safe(project.getTitle())
                );


        String technologies = "";


        if (project.getTechnologies() != null
                && !project.getTechnologies().isEmpty()) {

            technologies =
                    clean(
                            String.join(
                                    " | ",
                                    project.getTechnologies()
                            )
                    );
        }


        float titleWidth =
                getTextWidth(
                        title,
                        BOLD,
                        PROJECT_TITLE_SIZE
                );

        float technologyWidth =
                getTextWidth(
                        technologies,
                        ITALIC,
                        PROJECT_TECH_SIZE
                );


        /*
         * -----------------------------------------------------
         * PROJECT HEADER
         *
         * HOD Management System                 Java | Spring...
         * -----------------------------------------------------
         */

        if (hasText(technologies)
                && titleWidth
                + technologyWidth
                + 25f
                <= CONTENT_WIDTH) {

            drawText(
                    content,
                    title,
                    LEFT_MARGIN,
                    ctx.y,
                    BOLD,
                    PROJECT_TITLE_SIZE,
                    BLACK
            );


            float techX =
                    PAGE_WIDTH
                            - RIGHT_MARGIN
                            - technologyWidth;


            drawText(
                    content,
                    technologies,
                    techX,
                    ctx.y,
                    ITALIC,
                    PROJECT_TECH_SIZE,
                    BLACK
            );


            ctx.y -= 11f;

        } else {

            drawText(
                    content,
                    title,
                    LEFT_MARGIN,
                    ctx.y,
                    BOLD,
                    PROJECT_TITLE_SIZE,
                    BLACK
            );

            ctx.y -= 11f;


            if (hasText(technologies)) {

                writeWrappedText(
                        content,
                        ctx,
                        technologies,
                        LEFT_MARGIN,
                        PROJECT_TECH_SIZE,
                        ITALIC,
                        1.2f
                );
            }
        }


        // =====================================================
        // PROJECT BULLETS
        // =====================================================

        if (project.getHighlights() != null) {

            for (String highlight :
                    project.getHighlights()) {

                if (hasText(highlight)) {

                    writeBullet(
                            content,
                            ctx,
                            highlight,
                            BULLET_SIZE
                    );
                }
            }
        }
    }


    // =========================================================
    // EDUCATION
    // =========================================================

    private void writeEducation(
            PDPageContentStream content,
            PageContext ctx,
            EducationDTO education
    ) throws IOException {

        if (education == null) {
            return;
        }


        ensureSpace(
                ctx,
                22f
        );


        String institution =
                clean(
                        safe(education.getInstitution())
                );


        String degree =
                clean(
                        safe(education.getDegree())
                );


        String startDate =
                clean(
                        safe(education.getStartDate())
                );


        String endDate =
                clean(
                        safe(education.getEndDate())
                );


        String dates = "";


        if (hasText(startDate)
                && hasText(endDate)) {

            dates =
                    startDate
                            + " - "
                            + endDate;

        } else if (hasText(startDate)) {

            dates =
                    startDate;

        } else if (hasText(endDate)) {

            dates =
                    endDate;
        }


        // =====================================================
        // INSTITUTION
        // =====================================================

        float dateWidth =
                getTextWidth(
                        dates,
                        ITALIC,
                        DATE_SIZE
                );


        drawText(
                content,
                institution,
                LEFT_MARGIN,
                ctx.y,
                BOLD,
                EDUCATION_INSTITUTION_SIZE,
                BLACK
        );


        // =====================================================
        // DATE RIGHT ALIGNED
        // =====================================================

        if (hasText(dates)) {

            float dateX =
                    PAGE_WIDTH
                            - RIGHT_MARGIN
                            - dateWidth;


            drawText(
                    content,
                    dates,
                    dateX,
                    ctx.y,
                    ITALIC,
                    DATE_SIZE,
                    BLACK
            );
        }


        ctx.y -= 11f;


        // =====================================================
        // DEGREE
        // =====================================================

        if (hasText(degree)) {

            writeWrappedText(
                    content,
                    ctx,
                    degree,
                    LEFT_MARGIN,
                    EDUCATION_DETAILS_SIZE,
                    REGULAR,
                    1.2f
            );
        }
    }


    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperience(
            PDPageContentStream content,
            PageContext ctx,
            ExperienceDTO experience
    ) throws IOException {

        if (experience == null) {
            return;
        }


        ensureSpace(
                ctx,
                20f
        );


        String role =
                clean(
                        safe(experience.getRole())
                );


        String company =
                clean(
                        safe(experience.getCompany())
                );


        String heading =
                role;


        if (hasText(company)) {

            if (hasText(heading)) {

                heading +=
                        " - " + company;

            } else {

                heading =
                        company;
            }
        }


        // =====================================================
        // ROLE + COMPANY
        // =====================================================

        if (hasText(heading)) {

            drawText(
                    content,
                    heading,
                    LEFT_MARGIN,
                    ctx.y,
                    BOLD,
                    EDUCATION_INSTITUTION_SIZE,
                    BLACK
            );

            ctx.y -= 11f;
        }


        // =====================================================
        // DURATION
        // =====================================================

        if (hasText(
                experience.getDuration()
        )) {

            writeWrappedText(
                    content,
                    ctx,
                    clean(
                            experience.getDuration()
                    ),
                    LEFT_MARGIN,
                    EDUCATION_DETAILS_SIZE,
                    REGULAR,
                    1.2f
            );
        }


        // =====================================================
        // DESCRIPTION
        // =====================================================

        if (hasText(
                experience.getDescription()
        )) {

            writeBullet(
                    content,
                    ctx,
                    experience.getDescription(),
                    BULLET_SIZE
            );
        }
    }


    // =========================================================
    // TECHNICAL SKILLS
    // =========================================================

    private void writeSkillLine(
            PDPageContentStream content,
            PageContext ctx,
            String category,
            List<String> skills
    ) throws IOException {

        if (skills == null
                || skills.isEmpty()) {

            return;
        }


        String skillText =
                clean(
                        String.join(
                                ", ",
                                skills
                        )
                );


        if (!hasText(skillText)) {
            return;
        }


        ensureSpace(
                ctx,
                10f
        );


        /*
         * Fixed label column.
         *
         * This is important for matching the reference:
         *
         * Languages:       Java, Python, SQL
         * Frameworks:       Spring Boot, React.js
         * Databases:       MySQL, JDBC
         * DevOps & Tools:  Git, GitHub, Docker
         * Concepts:        REST APIs, CRUD...
         */

        float labelWidth =
                getTextWidth(
                        "DevOps & Tools:",
                        BOLD,
                        SKILLS_SIZE
                );


        float skillX =
                LEFT_MARGIN
                        + labelWidth
                        + 12f;


        // =====================================================
        // CATEGORY
        // =====================================================

        drawText(
                content,
                category,
                LEFT_MARGIN,
                ctx.y,
                BOLD,
                SKILLS_SIZE,
                BLACK
        );


        // =====================================================
        // VALUES
        // =====================================================

        writeWrappedText(
                content,
                ctx,
                skillText,
                skillX,
                SKILLS_SIZE,
                REGULAR,
                1.1f
        );
    }


    // =========================================================
    // BULLET
    // =========================================================

    private void writeBullet(
            PDPageContentStream content,
            PageContext ctx,
            String text,
            float fontSize
    ) throws IOException {

        if (!hasText(text)) {
            return;
        }


        ensureSpace(
                ctx,
                fontSize + 3f
        );


        float bulletX =
                LEFT_MARGIN + 7f;

        float textX =
                LEFT_MARGIN + 17f;


        /*
         * Bullet
         */

        drawText(
                content,
                "•",
                bulletX,
                ctx.y,
                REGULAR,
                fontSize,
                BLACK
        );


        /*
         * Text
         */

        writeWrappedText(
                content,
                ctx,
                text,
                textX,
                fontSize,
                REGULAR,
                1.5f
        );
    }


    // =========================================================
    // WRAPPED TEXT
    // =========================================================

    private void writeWrappedText(
            PDPageContentStream content,
            PageContext ctx,
            String text,
            float x,
            float fontSize,
            PDType1Font font,
            float extraSpacing
    ) throws IOException {

        if (!hasText(text)) {
            return;
        }


        String cleaned =
                clean(text);


        String[] words =
                cleaned.split("\\s+");


        StringBuilder line =
                new StringBuilder();


        float availableWidth =
                PAGE_WIDTH
                        - RIGHT_MARGIN
                        - x;


        for (String word : words) {

            String testLine;


            if (line.isEmpty()) {

                testLine =
                        word;

            } else {

                testLine =
                        line
                                + " "
                                + word;
            }


            float testWidth =
                    getTextWidth(
                            testLine,
                            font,
                            fontSize
                    );


            if (testWidth > availableWidth
                    && !line.isEmpty()) {

                writeLine(
                        content,
                        ctx,
                        line.toString(),
                        x,
                        fontSize,
                        font,
                        extraSpacing
                );


                line =
                        new StringBuilder(
                                word
                        );

            } else {

                line =
                        new StringBuilder(
                                testLine
                        );
            }
        }


        if (!line.isEmpty()) {

            writeLine(
                    content,
                    ctx,
                    line.toString(),
                    x,
                    fontSize,
                    font,
                    extraSpacing
            );
        }
    }


    // =========================================================
    // WRITE ONE LINE
    // =========================================================

    private void writeLine(
            PDPageContentStream content,
            PageContext ctx,
            String text,
            float x,
            float fontSize,
            PDType1Font font,
            float extraSpacing
    ) throws IOException {

        ensureSpace(
                ctx,
                fontSize + extraSpacing + 1f
        );


        drawText(
                content,
                text,
                x,
                ctx.y,
                font,
                fontSize,
                BLACK
        );


        ctx.y -=
                fontSize
                        + extraSpacing;
    }


    // =========================================================
    // DRAW TEXT
    // =========================================================

    private void drawText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            PDType1Font font,
            float fontSize,
            Color color
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
                color
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


    // =========================================================
    // TEXT WIDTH
    // =========================================================

    private float getTextWidth(
            String text,
            PDType1Font font,
            float fontSize
    ) {

        if (!hasText(text)) {
            return 0f;
        }


        try {

            return font.getStringWidth(
                    clean(text)
            ) / 1000f * fontSize;

        } catch (IOException e) {

            return 0f;
        }
    }


    // =========================================================
    // PAGE SPACE
    // =========================================================

    private void ensureSpace(
            PageContext ctx,
            float requiredHeight
    ) {

        if (ctx.y - requiredHeight
                < BOTTOM_MARGIN) {

            throw new RuntimeException(
                    "Resume content exceeds one A4 page. " +
                            "Please reduce resume content."
            );
        }
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    // =========================================================
    // HAS TEXT
    // =========================================================

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }


    // =========================================================
    // CLEAN TEXT
    // =========================================================

    private String clean(
            String value
    ) {

        if (value == null) {
            return "";
        }


        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("•", "•")
                .replaceAll("\\s+", " ")
                .trim();
    }
}