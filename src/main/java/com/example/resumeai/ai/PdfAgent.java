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
    // PAGE SETTINGS
    // =========================================================

    private static final float PAGE_WIDTH =
            PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT =
            PDRectangle.A4.getHeight();

    /*
     * These margins are intentionally compact.
     * They are tuned for the reference resume layout.
     */
    private static final float MARGIN_LEFT = 52f;

    private static final float MARGIN_RIGHT = 52f;

    private static final float TOP_MARGIN = 36f;

    private static final float BOTTOM_MARGIN = 30f;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    /*
     * Current vertical cursor.
     */
    private float yPosition;


    // =========================================================
    // FONTS
    // =========================================================

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


    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BLACK =
            Color.BLACK;

    private static final Color LINK_BLUE =
            new Color(0, 70, 180);


    // =========================================================
    // MAIN PDF GENERATOR
    // =========================================================

    /*
     * synchronized is intentional.
     *
     * PdfAgent is a Spring singleton and yPosition is an
     * instance field. synchronized prevents two simultaneous
     * requests from modifying the same yPosition.
     */
    public synchronized byte[] generateResumePdf(
            ImprovedResumeDTO resume
    ) {

        if (resume == null) {

            throw new IllegalArgumentException(
                    "Resume data cannot be null."
            );
        }

        try (PDDocument document = new PDDocument()) {

            // =================================================
            // CREATE ONE A4 PAGE
            // =================================================

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

                if (hasText(resume.getFullName())) {

                    writeCentered(
                            content,
                            resume.getFullName(),
                            18f,
                            FONT_BOLD
                    );

                    yPosition -= 2f;
                }


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

                yPosition -= 2f;


                // =================================================
                // SUMMARY
                // =================================================

                if (hasText(
                        resume.getProfessionalSummary()
                )) {

                    writeSectionTitle(
                            content,
                            "Summary"
                    );

                    writeWrappedText(
                            content,
                            resume.getProfessionalSummary(),
                            9f
                    );

                    yPosition -= 2f;
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

                        if (project == null) {
                            continue;
                        }

                        writeProjectHeader(
                                content,
                                project
                        );

                        if (project.getHighlights() != null) {

                            for (String highlight :
                                    project.getHighlights()) {

                                if (hasText(highlight)) {

                                    writeBullet(
                                            content,
                                            highlight
                                    );
                                }
                            }
                        }

                        // Compact gap between projects
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

                        if (education == null) {
                            continue;
                        }

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

                /*
                 * Reference resume does not contain an Experience
                 * section. Therefore this section is generated
                 * ONLY when actual experience data exists.
                 */
                if (resume.getExperience() != null
                        && !resume.getExperience().isEmpty()) {

                    boolean hasExperience = false;

                    for (ExperienceDTO experience :
                            resume.getExperience()) {

                        if (experience != null
                                && (
                                hasText(experience.getRole())
                                        || hasText(experience.getCompany())
                                        || hasText(experience.getDescription())
                        )) {

                            hasExperience = true;
                            break;
                        }
                    }

                    if (hasExperience) {

                        writeSectionTitle(
                                content,
                                "Experience"
                        );

                        for (ExperienceDTO experience :
                                resume.getExperience()) {

                            if (experience == null) {
                                continue;
                            }

                            writeExperience(
                                    content,
                                    experience
                            );

                            yPosition -= 1.5f;
                        }
                    }
                }


                // =================================================
                // CERTIFICATIONS
                // =================================================

                if (resume.getCertifications() != null
                        && !resume.getCertifications().isEmpty()) {

                    boolean hasCertification = false;

                    for (String certification :
                            resume.getCertifications()) {

                        if (hasText(certification)) {
                            hasCertification = true;
                            break;
                        }
                    }

                    if (hasCertification) {

                        writeSectionTitle(
                                content,
                                "Certifications"
                        );

                        for (String certification :
                                resume.getCertifications()) {

                            if (hasText(certification)) {

                                writeBullet(
                                        content,
                                        certification
                                );
                            }
                        }

                        yPosition -= 1f;
                    }
                }


                // =================================================
                // TECHNICAL SKILLS
                // =================================================

                SkillsDTO skills =
                        resume.getSkills();

                if (skills != null) {

                    boolean hasSkills =
                            hasList(skills.getLanguages())
                                    || hasList(skills.getFrameworks())
                                    || hasList(skills.getDatabases())
                                    || hasList(skills.getDevopsAndTools())
                                    || hasList(skills.getConcepts());

                    if (hasSkills) {

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
            }


            // =================================================
            // SAVE
            // =================================================

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


    // =========================================================
    // SECTION TITLE
    // =========================================================

    private void writeSectionTitle(
            PDPageContentStream content,
            String title
    ) throws IOException {

        if (!hasText(title)) {
            return;
        }

        ensureSpace(15f);

        /*
         * Small spacing before section.
         */
        yPosition -= 1f;


        // =====================================================
        // TITLE
        // =====================================================

        content.beginText();

        content.setFont(
                FONT_BOLD,
                10.5f
        );

        content.setNonStrokingColor(
                BLACK
        );

        content.newLineAtOffset(
                MARGIN_LEFT,
                yPosition
        );

        content.showText(
                clean(title)
        );

        content.endText();


        // =====================================================
        // HORIZONTAL LINE
        // =====================================================

        float titleWidth =
                getTextWidth(
                        title,
                        FONT_BOLD,
                        10.5f
                );

        float lineY =
                yPosition - 2.5f;

        content.setLineWidth(
                0.5f
        );

        content.setStrokingColor(
                BLACK
        );

        content.moveTo(
                MARGIN_LEFT + titleWidth + 6f,
                lineY
        );

        content.lineTo(
                PAGE_WIDTH - MARGIN_RIGHT,
                lineY
        );

        content.stroke();


        /*
         * Space after heading.
         */
        yPosition -= 12f;
    }


    // =========================================================
    // CENTERED NAME
    // =========================================================

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
                (PAGE_WIDTH - textWidth) / 2f;


        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.setNonStrokingColor(
                BLACK
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
                fontSize + 1f;
    }


    // =========================================================
    // CONTACT INFORMATION
    // =========================================================

    private void writeContactInfo(
            PDDocument document,
            PDPage page,
            PDPageContentStream content,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        final float fontSize = 8.5f;

        String emailText =
                clean(safe(email));

        String linkedinText =
                clean(safe(linkedin));

        String githubText =
                clean(safe(github));

        String separator =
                "  —  ";


        // =====================================================
        // WIDTHS
        // =====================================================

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


        // =====================================================
        // COUNT
        // =====================================================

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


        // =====================================================
        // TOTAL WIDTH
        // =====================================================

        float totalWidth =
                emailWidth
                        + linkedinWidth
                        + githubWidth
                        + Math.max(
                        0,
                        count - 1
                ) * separatorWidth;


        float x =
                (PAGE_WIDTH - totalWidth) / 2f;


        // =====================================================
        // EMAIL
        // =====================================================

        if (hasText(emailText)) {

            String emailUrl =
                    "mailto:" + emailText;

            drawContactLink(
                    document,
                    page,
                    content,
                    emailText,
                    x,
                    yPosition,
                    emailWidth,
                    fontSize,
                    emailUrl
            );

            x += emailWidth;
        }


        // =====================================================
        // LINKEDIN
        // =====================================================

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
                    normalizeUrl(linkedinText)
            );

            x += linkedinWidth;
        }


        // =====================================================
        // GITHUB
        // =====================================================

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
                    normalizeUrl(githubText)
            );
        }


        yPosition -= 10f;
    }


    // =========================================================
    // CONTACT LINK
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

        if (!hasText(text)) {
            return;
        }


        // =====================================================
        // TEXT
        // =====================================================

        content.beginText();

        content.setFont(
                FONT_REGULAR,
                fontSize
        );

        content.setNonStrokingColor(
                LINK_BLUE
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                clean(text)
        );

        content.endText();


        // =====================================================
        // RESET COLOR
        // =====================================================

        content.setNonStrokingColor(
                BLACK
        );


        // =====================================================
        // CLICKABLE LINK
        // =====================================================

        if (hasText(url)) {

            PDAnnotationLink link =
                    new PDAnnotationLink();

            PDRectangle rectangle =
                    new PDRectangle();

            rectangle.setLowerLeftX(
                    x
            );

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
                    PDAnnotationLink
                            .HIGHLIGHT_MODE_NONE
            );

            PDActionURI action =
                    new PDActionURI();

            action.setURI(
                    url
            );

            link.setAction(
                    action
            );

            page.getAnnotations()
                    .add(link);
        }
    }


    // =========================================================
    // PROJECT HEADER
    // =========================================================

    private void writeProjectHeader(
            PDPageContentStream content,
            ProjectDTO project
    ) throws IOException {

        ensureSpace(13f);

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


        final float titleFontSize = 10f;

        final float techFontSize = 8.5f;


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


        // =====================================================
        // TITLE + TECHNOLOGIES SAME LINE
        // =====================================================

        if (hasText(technologies)
                && titleWidth
                + techWidth
                + 20f
                <= CONTENT_WIDTH) {

            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    titleFontSize
            );


            // -------------------------------------------------
            // TECHNOLOGIES
            // -------------------------------------------------

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


            yPosition -= 11.5f;
        }


        // =====================================================
        // IF TOO LONG
        // =====================================================

        else {

            drawNormalText(
                    content,
                    title,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    titleFontSize
            );

            yPosition -= 11f;

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


    // =========================================================
    // EDUCATION
    // =========================================================

    private void writeEducation(
            PDPageContentStream content,
            EducationDTO education
    ) throws IOException {

        ensureSpace(24f);

        final float institutionFontSize = 9.5f;

        final float detailFontSize = 8.5f;


        String institution =
                clean(
                        safe(
                                education.getInstitution()
                        )
                );


        String startDate =
                clean(
                        safe(
                                education.getStartDate()
                        )
                );

        String endDate =
                clean(
                        safe(
                                education.getEndDate()
                        )
                );


        String dates =
                buildDateRange(
                        startDate,
                        endDate
                );


        // =====================================================
        // INSTITUTION
        // =====================================================

        drawNormalText(
                content,
                institution,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                institutionFontSize
        );


        // =====================================================
        // DATE
        // =====================================================

        if (hasText(dates)) {

            float dateWidth =
                    getTextWidth(
                            dates,
                            FONT_ITALIC,
                            8.5f
                    );

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


        yPosition -= 11f;


        // =====================================================
        // DEGREE + MAJOR
        // =====================================================

        String degree =
                clean(
                        safe(
                                education.getDegree()
                        )
                );

        String major =
                clean(
                        safe(
                                education.getMajor()
                        )
                );


        String educationDetail =
                degree;


        if (hasText(major)) {

            if (hasText(educationDetail)) {

                educationDetail +=
                        " - " + major;

            } else {

                educationDetail =
                        major;
            }
        }


        if (hasText(educationDetail)) {

            writeWrappedText(
                    content,
                    educationDetail,
                    detailFontSize
            );
        }
    }


    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperience(
            PDPageContentStream content,
            ExperienceDTO experience
    ) throws IOException {

        ensureSpace(25f);

        String role =
                clean(
                        safe(
                                experience.getRole()
                        )
                );

        String company =
                clean(
                        safe(
                                experience.getCompany()
                        )
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
        // ROLE / COMPANY
        // =====================================================

        if (hasText(heading)) {

            drawNormalText(
                    content,
                    heading,
                    MARGIN_LEFT,
                    yPosition,
                    FONT_BOLD,
                    9.5f
            );

            yPosition -= 11f;
        }


        // =====================================================
        // DURATION
        // =====================================================

        if (hasText(
                experience.getDuration()
        )) {

            writeWrappedText(
                    content,
                    experience.getDuration(),
                    8.5f
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
                    experience.getDescription()
            );
        }
    }


    // =========================================================
    // TECHNICAL SKILLS
    // =========================================================

    private void writeSkillLine(
            PDPageContentStream content,
            String category,
            List<String> skills
    ) throws IOException {

        if (!hasList(skills)) {
            return;
        }


        String skillText =
                clean(
                        String.join(
                                ", ",
                                skills
                        )
                );


        final float fontSize = 8.5f;


        // =====================================================
        // CATEGORY
        // =====================================================

        drawNormalText(
                content,
                category,
                MARGIN_LEFT,
                yPosition,
                FONT_BOLD,
                fontSize
        );


        // =====================================================
        // FIXED LABEL COLUMN
        // =====================================================

        float categoryWidth =
                getTextWidth(
                        "DevOps & Tools:",
                        FONT_BOLD,
                        fontSize
                );


        float skillX =
                MARGIN_LEFT
                        + categoryWidth
                        + 10f;


        // =====================================================
        // SKILLS
        // =====================================================

        writeWrappedTextAtX(
                content,
                skillText,
                skillX,
                fontSize
        );
    }


    // =========================================================
    // BULLET
    // =========================================================

    private void writeBullet(
            PDPageContentStream content,
            String text
    ) throws IOException {

        if (!hasText(text)) {
            return;
        }

        final float fontSize = 8.7f;


        ensureSpace(12f);


        // =====================================================
        // BULLET SYMBOL
        // =====================================================

        drawNormalText(
                content,
                "•",
                MARGIN_LEFT + 5f,
                yPosition,
                FONT_REGULAR,
                fontSize
        );


        // =====================================================
        // BULLET TEXT
        // =====================================================

        writeWrappedTextAtX(
                content,
                text,
                MARGIN_LEFT + 16f,
                fontSize
        );
    }


    // =========================================================
    // WRAPPED TEXT
    // =========================================================

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


    // =========================================================
    // WRAPPED TEXT AT X
    // =========================================================

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


    // =========================================================
    // WRAPPED TEXT WITH FONT
    // =========================================================

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
                clean(text)
                        .replace("\n", " ")
                        .replace("\r", " ")
                        .replaceAll("\\s+", " ")
                        .trim();


        if (cleanText.isEmpty()) {
            return;
        }


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


            // =================================================
            // LINE TOO LONG
            // =================================================

            if (testWidth > availableWidth
                    && !line.isEmpty()) {

                writeLineAtX(
                        content,
                        line.toString(),
                        x,
                        fontSize,
                        font
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


        // =====================================================
        // LAST LINE
        // =====================================================

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


    // =========================================================
    // WRITE LINE
    // =========================================================

    private void writeLineAtX(
            PDPageContentStream content,
            String text,
            float x,
            float fontSize,
            PDType1Font font
    ) throws IOException {

        ensureSpace(
                fontSize + 2f
        );


        drawNormalText(
                content,
                text,
                x,
                yPosition,
                font,
                fontSize
        );


        /*
         * Compact line spacing.
         *
         * This is one of the main changes responsible
         * for keeping the resume on one A4 page.
         */
        yPosition -=
                fontSize + 1.6f;
    }


    // =========================================================
    // DRAW NORMAL TEXT
    // =========================================================

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
                BLACK
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
            float requiredHeight
    ) {

        if (yPosition - requiredHeight
                < BOTTOM_MARGIN) {

            throw new RuntimeException(
                    "Resume content exceeds one A4 page. " +
                            "Please reduce resume content."
            );
        }
    }


    // =========================================================
    // DATE RANGE
    // =========================================================

    private String buildDateRange(
            String startDate,
            String endDate
    ) {

        boolean hasStart =
                hasText(startDate);

        boolean hasEnd =
                hasText(endDate);


        if (hasStart && hasEnd) {

            return startDate
                    + " - "
                    + endDate;
        }


        if (hasStart) {

            return startDate;
        }


        if (hasEnd) {

            return endDate;
        }


        return "";
    }


    // =========================================================
    // URL NORMALIZATION
    // =========================================================

    private String normalizeUrl(
            String value
    ) {

        if (!hasText(value)) {
            return "";
        }


        String url =
                value.trim();


        if (url.startsWith("http://")
                || url.startsWith("https://")
                || url.startsWith("mailto:")) {

            return url;
        }


        return "https://" + url;
    }


    // =========================================================
    // HAS LIST
    // =========================================================

    private boolean hasList(
            List<String> list
    ) {

        if (list == null
                || list.isEmpty()) {

            return false;
        }


        for (String item : list) {

            if (hasText(item)) {
                return true;
            }
        }


        return false;
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
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("•", "•")
                .trim();
    }
}