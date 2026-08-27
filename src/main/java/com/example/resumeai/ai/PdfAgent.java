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

    private static final float LEFT = 38f;
    private static final float RIGHT = 38f;
    private static final float TOP = 35f;
    private static final float BOTTOM = 30f;

    // Two-column layout
    private static final float LEFT_COLUMN_WIDTH = 155f;
    private static final float COLUMN_GAP = 25f;

    private static final float RIGHT_COLUMN_X =
            LEFT + LEFT_COLUMN_WIDTH + COLUMN_GAP;

    private static final float RIGHT_COLUMN_WIDTH =
            PAGE_WIDTH - RIGHT - RIGHT_COLUMN_X;

    // =========================================================
    // FONTS
    // =========================================================

    private static final PDType1Font REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final PDType1Font ITALIC =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BLACK = Color.BLACK;

    private static final Color DARK =
            new Color(45, 45, 45);

    private static final Color GRAY =
            new Color(95, 95, 95);

    private static final Color LIGHT_GRAY =
            new Color(190, 190, 190);

    private static final Color ACCENT =
            new Color(35, 65, 95);

    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float NAME_SIZE = 22f;
    private static final float CONTACT_SIZE = 8.5f;

    private static final float SECTION_SIZE = 10.5f;

    private static final float SUMMARY_SIZE = 8.8f;

    private static final float PROJECT_TITLE_SIZE = 9.5f;
    private static final float PROJECT_TEXT_SIZE = 8.5f;

    private static final float EXPERIENCE_TITLE_SIZE = 9.5f;

    private static final float BODY_SIZE = 8.5f;

    private static final float SKILL_SIZE = 8.3f;

    private static final float EDUCATION_TITLE_SIZE = 9f;
    private static final float EDUCATION_TEXT_SIZE = 8.2f;

    // =========================================================
    // SPACING
    // =========================================================

    private static final float HEADER_BOTTOM = 15f;

    private static final float SECTION_TOP = 12f;
    private static final float SECTION_BOTTOM = 6f;

    private static final float BLOCK_GAP = 5f;

    private static final float BODY_LEADING = 11f;

    // =========================================================
    // PAGE CONTEXT
    // =========================================================

    private static class ColumnContext {

        float y;

        ColumnContext(float y) {
            this.y = y;
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public byte[] generateResumePdf(ImprovedResumeDTO resume) {

        if (resume == null) {
            throw new IllegalArgumentException(
                    "Resume data cannot be null."
            );
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            try (PDPageContentStream cs =
                         new PDPageContentStream(document, page)) {

                // -------------------------------------------------
                // HEADER
                // -------------------------------------------------

                float headerY = PAGE_HEIGHT - TOP;

                writeHeader(
                        document,
                        page,
                        cs,
                        resume,
                        headerY
                );

                // -------------------------------------------------
                // COLUMNS
                // -------------------------------------------------

                float contentTop =
                        headerY - HEADER_BOTTOM;

                ColumnContext left =
                        new ColumnContext(contentTop);

                ColumnContext right =
                        new ColumnContext(contentTop);

                // -------------------------------------------------
                // LEFT COLUMN
                // -------------------------------------------------

                writeLeftColumn(
                        document,
                        page,
                        cs,
                        resume,
                        left
                );

                // -------------------------------------------------
                // RIGHT COLUMN
                // -------------------------------------------------

                writeRightColumn(
                        cs,
                        resume,
                        right
                );

                // -------------------------------------------------
                // COLUMN DIVIDER
                // -------------------------------------------------

                drawColumnDivider(
                        cs,
                        contentTop
                );
            }

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
    // HEADER
    // =========================================================

    private void writeHeader(
            PDDocument document,
            PDPage page,
            PDPageContentStream cs,
            ImprovedResumeDTO resume,
            float y
    ) throws IOException {

        String name =
                clean(safe(resume.getFullName()));

        if (hasText(name)) {

            float nameWidth =
                    getTextWidth(
                            name,
                            BOLD,
                            NAME_SIZE
                    );

            float nameX =
                    (PAGE_WIDTH - nameWidth) / 2f;

            drawText(
                    cs,
                    name,
                    nameX,
                    y,
                    BOLD,
                    NAME_SIZE,
                    BLACK
            );

            y -= 18f;
        }

        // Contact row

        String email =
                clean(safe(resume.getEmail()));

        String linkedin =
                clean(safe(resume.getLinkedin()));

        String github =
                clean(safe(resume.getGithub()));

        String separator = "  |  ";

        StringBuilder contact =
                new StringBuilder();

        if (hasText(email)) {
            contact.append(email);
        }

        if (hasText(linkedin)) {

            if (contact.length() > 0) {
                contact.append(separator);
            }

            contact.append(linkedin);
        }

        if (hasText(github)) {

            if (contact.length() > 0) {
                contact.append(separator);
            }

            contact.append(github);
        }

        String contactText =
                contact.toString();

        if (hasText(contactText)) {

            float contactWidth =
                    getTextWidth(
                            contactText,
                            REGULAR,
                            CONTACT_SIZE
                    );

            float contactX =
                    (PAGE_WIDTH - contactWidth) / 2f;

            drawText(
                    cs,
                    contactText,
                    contactX,
                    y,
                    REGULAR,
                    CONTACT_SIZE,
                    GRAY
            );

            // Clickable links
            createContactLinks(
                    document,
                    page,
                    contactText,
                    contactX,
                    y,
                    email,
                    linkedin,
                    github
            );

            y -= 13f;
        }

        // Header line

        cs.setStrokingColor(ACCENT);
        cs.setLineWidth(1.1f);

        cs.moveTo(
                LEFT,
                y
        );

        cs.lineTo(
                PAGE_WIDTH - RIGHT,
                y
        );

        cs.stroke();
    }

    // =========================================================
    // CONTACT LINKS
    // =========================================================

    private void createContactLinks(
            PDDocument document,
            PDPage page,
            String fullText,
            float startX,
            float y,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        String separator = "  |  ";

        float currentX = startX;

        if (hasText(email)) {

            float width =
                    getTextWidth(
                            email,
                            REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    email,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    "mailto:" + email
            );

            currentX += width;
        }

        if (hasText(linkedin)) {

            if (currentX > startX) {

                currentX +=
                        getTextWidth(
                                separator,
                                REGULAR,
                                CONTACT_SIZE
                        );
            }

            float width =
                    getTextWidth(
                            linkedin,
                            REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    linkedin,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    normalizeUrl(linkedin)
            );

            currentX += width;
        }

        if (hasText(github)) {

            if (currentX > startX) {

                currentX +=
                        getTextWidth(
                                separator,
                                REGULAR,
                                CONTACT_SIZE
                        );
            }

            float width =
                    getTextWidth(
                            github,
                            REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    github,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    normalizeUrl(github)
            );
        }
    }

    // =========================================================
    // LEFT COLUMN
    // =========================================================

    private void writeLeftColumn(
            PDDocument document,
            PDPage page,
            PDPageContentStream cs,
            ImprovedResumeDTO resume,
            ColumnContext ctx
    ) throws IOException {

        SkillsDTO skills =
                resume.getSkills();

        // -------------------------------------------------
        // SKILLS
        // -------------------------------------------------

        if (skills != null) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "TECHNICAL SKILLS",
                    LEFT
            );

            writeSkillBlock(
                    cs,
                    ctx,
                    "Languages",
                    skills.getLanguages()
            );

            writeSkillBlock(
                    cs,
                    ctx,
                    "Frameworks",
                    skills.getFrameworks()
            );

            writeSkillBlock(
                    cs,
                    ctx,
                    "Databases",
                    skills.getDatabases()
            );

            writeSkillBlock(
                    cs,
                    ctx,
                    "DevOps & Tools",
                    skills.getDevopsAndTools()
            );

            writeSkillBlock(
                    cs,
                    ctx,
                    "Concepts",
                    skills.getConcepts()
            );
        }

        // -------------------------------------------------
        // EDUCATION
        // -------------------------------------------------

        if (resume.getEducation() != null &&
                !resume.getEducation().isEmpty()) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "EDUCATION",
                    LEFT
            );

            for (EducationDTO education :
                    resume.getEducation()) {

                writeEducationCompact(
                        cs,
                        ctx,
                        education
                );
            }
        }

        // -------------------------------------------------
        // CERTIFICATIONS
        // -------------------------------------------------

        if (resume.getCertifications() != null &&
                !resume.getCertifications().isEmpty()) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "CERTIFICATIONS",
                    LEFT
            );

            for (String certification :
                    resume.getCertifications()) {

                if (hasText(certification)) {

                    writeLeftBullet(
                            cs,
                            ctx,
                            certification
                    );
                }
            }
        }
    }

    // =========================================================
    // RIGHT COLUMN
    // =========================================================

    private void writeRightColumn(
            PDPageContentStream cs,
            ImprovedResumeDTO resume,
            ColumnContext ctx
    ) throws IOException {

        // -------------------------------------------------
        // SUMMARY
        // -------------------------------------------------

        if (hasText(
                resume.getProfessionalSummary()
        )) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "PROFESSIONAL SUMMARY",
                    RIGHT_COLUMN_X
            );

            writeWrapped(
                    cs,
                    ctx,
                    resume.getProfessionalSummary(),
                    RIGHT_COLUMN_X,
                    RIGHT_COLUMN_WIDTH,
                    SUMMARY_SIZE,
                    REGULAR,
                    BODY_LEADING
            );
        }

        // -------------------------------------------------
        // EXPERIENCE
        // -------------------------------------------------

        if (resume.getExperience() != null &&
                !resume.getExperience().isEmpty()) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "EXPERIENCE",
                    RIGHT_COLUMN_X
            );

            for (ExperienceDTO experience :
                    resume.getExperience()) {

                writeExperienceBlock(
                        cs,
                        ctx,
                        experience
                );
            }
        }

        // -------------------------------------------------
        // PROJECTS
        // -------------------------------------------------

        if (resume.getProjects() != null &&
                !resume.getProjects().isEmpty()) {

            writeSectionTitle(
                    cs,
                    ctx,
                    "PROJECTS",
                    RIGHT_COLUMN_X
            );

            for (ProjectDTO project :
                    resume.getProjects()) {

                writeProjectBlock(
                        cs,
                        ctx,
                        project
                );
            }
        }
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private void writeSectionTitle(
            PDPageContentStream cs,
            ColumnContext ctx,
            String title,
            float x
    ) throws IOException {

        ctx.y -= SECTION_TOP;

        drawText(
                cs,
                title,
                x,
                ctx.y,
                BOLD,
                SECTION_SIZE,
                ACCENT
        );

        float titleWidth =
                getTextWidth(
                        title,
                        BOLD,
                        SECTION_SIZE
                );

        float lineY =
                ctx.y - 2.5f;

        cs.setStrokingColor(
                LIGHT_GRAY
        );

        cs.setLineWidth(
                0.5f
        );

        cs.moveTo(
                x + titleWidth + 7f,
                lineY
        );

        cs.lineTo(
                x + (
                        x == LEFT
                                ? LEFT_COLUMN_WIDTH
                                : RIGHT_COLUMN_WIDTH
                ),
                lineY
        );

        cs.stroke();

        ctx.y -= SECTION_BOTTOM;
    }

    // =========================================================
    // SKILLS
    // =========================================================

    private void writeSkillBlock(
            PDPageContentStream cs,
            ColumnContext ctx,
            String label,
            List<String> values
    ) throws IOException {

        if (values == null ||
                values.isEmpty()) {
            return;
        }

        String text =
                clean(
                        String.join(
                                ", ",
                                values
                        )
                );

        if (!hasText(text)) {
            return;
        }

        drawText(
                cs,
                label,
                LEFT,
                ctx.y,
                BOLD,
                SKILL_SIZE,
                DARK
        );

        ctx.y -= 10f;

        ctx.y = writeWrapped(
                cs,
                ctx,
                text,
                LEFT,
                LEFT_COLUMN_WIDTH,
                SKILL_SIZE,
                REGULAR,
                10f
        );

        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // EDUCATION
    // =========================================================

    private void writeEducationCompact(
            PDPageContentStream cs,
            ColumnContext ctx,
            EducationDTO education
    ) throws IOException {

        if (education == null) {
            return;
        }

        String institution =
                clean(
                        safe(
                                education.getInstitution()
                        )
                );

        String degree =
                clean(
                        safe(
                                education.getDegree()
                        )
                );

        String start =
                clean(
                        safe(
                                education.getStartDate()
                        )
                );

        String end =
                clean(
                        safe(
                                education.getEndDate()
                        )
                );

        drawText(
                cs,
                institution,
                LEFT,
                ctx.y,
                BOLD,
                EDUCATION_TITLE_SIZE,
                DARK
        );

        ctx.y -= 11f;

        if (hasText(degree)) {

            ctx.y = writeWrapped(
                    cs,
                    ctx,
                    degree,
                    LEFT,
                    LEFT_COLUMN_WIDTH,
                    EDUCATION_TEXT_SIZE,
                    REGULAR,
                    9.5f
            );
        }

        if (hasText(start) ||
                hasText(end)) {

            String dates = start;

            if (hasText(start) &&
                    hasText(end)) {

                dates =
                        start +
                                " - " +
                                end;
            } else if (hasText(end)) {

                dates = end;
            }

            ctx.y = writeWrapped(
                    cs,
                    ctx,
                    dates,
                    LEFT,
                    LEFT_COLUMN_WIDTH,
                    EDUCATION_TEXT_SIZE,
                    ITALIC,
                    9f
            );
        }

        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperienceBlock(
            PDPageContentStream cs,
            ColumnContext ctx,
            ExperienceDTO experience
    ) throws IOException {

        if (experience == null) {
            return;
        }

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

        String heading = role;

        if (hasText(company)) {

            if (hasText(heading)) {

                heading =
                        heading +
                                " | " +
                                company;

            } else {

                heading = company;
            }
        }

        if (hasText(heading)) {

            drawText(
                    cs,
                    heading,
                    RIGHT_COLUMN_X,
                    ctx.y,
                    BOLD,
                    EXPERIENCE_TITLE_SIZE,
                    DARK
            );

            ctx.y -= 11f;
        }

        if (hasText(
                experience.getDuration()
        )) {

            ctx.y = writeWrapped(
                    cs,
                    ctx,
                    clean(
                            experience.getDuration()
                    ),
                    RIGHT_COLUMN_X,
                    RIGHT_COLUMN_WIDTH,
                    EDUCATION_TEXT_SIZE,
                    ITALIC,
                    9f
            );
        }

        if (hasText(
                experience.getDescription()
        )) {

            ctx.y = writeBullet(
                    cs,
                    ctx,
                    experience.getDescription()
            );
        }

        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // PROJECT
    // =========================================================

    private void writeProjectBlock(
            PDPageContentStream cs,
            ColumnContext ctx,
            ProjectDTO project
    ) throws IOException {

        if (project == null) {
            return;
        }

        String title =
                clean(
                        safe(
                                project.getTitle()
                        )
                );

        if (hasText(title)) {

            drawText(
                    cs,
                    title,
                    RIGHT_COLUMN_X,
                    ctx.y,
                    BOLD,
                    PROJECT_TITLE_SIZE,
                    DARK
            );

            ctx.y -= 11f;
        }

        // Technologies

        if (project.getTechnologies() != null &&
                !project.getTechnologies().isEmpty()) {

            String technologies =
                    clean(
                            String.join(
                                    " • ",
                                    project.getTechnologies()
                            )
                    );

            ctx.y = writeWrapped(
                    cs,
                    ctx,
                    technologies,
                    RIGHT_COLUMN_X,
                    RIGHT_COLUMN_WIDTH,
                    PROJECT_TEXT_SIZE,
                    ITALIC,
                    9f
            );
        }

        // Highlights

        if (project.getHighlights() != null) {

            for (String highlight :
                    project.getHighlights()) {

                if (hasText(highlight)) {

                    ctx.y = writeBullet(
                            cs,
                            ctx,
                            highlight
                    );
                }
            }
        }

        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // LEFT BULLET
    // =========================================================

    private void writeLeftBullet(
            PDPageContentStream cs,
            ColumnContext ctx,
            String text
    ) throws IOException {

        float bulletX = LEFT;

        float textX = LEFT + 9f;

        drawText(
                cs,
                "•",
                bulletX,
                ctx.y,
                REGULAR,
                SKILL_SIZE,
                DARK
        );

        ctx.y = writeWrapped(
                cs,
                ctx,
                text,
                textX,
                LEFT_COLUMN_WIDTH - 9f,
                SKILL_SIZE,
                REGULAR,
                9.5f
        );

        ctx.y -= 2f;
    }

    // =========================================================
    // RIGHT BULLET
    // =========================================================

    private float writeBullet(
            PDPageContentStream cs,
            ColumnContext ctx,
            String text
    ) throws IOException {

        float bulletX =
                RIGHT_COLUMN_X;

        float textX =
                RIGHT_COLUMN_X + 9f;

        drawText(
                cs,
                "•",
                bulletX,
                ctx.y,
                REGULAR,
                BODY_SIZE,
                DARK
        );

        return writeWrapped(
                cs,
                ctx,
                text,
                textX,
                RIGHT_COLUMN_WIDTH - 9f,
                BODY_SIZE,
                REGULAR,
                BODY_LEADING
        );
    }

    // =========================================================
    // WRAPPED TEXT
    // =========================================================

    private float writeWrapped(
            PDPageContentStream cs,
            ColumnContext ctx,
            String text,
            float x,
            float width,
            float fontSize,
            PDType1Font font,
            float leading
    ) throws IOException {

        if (!hasText(text)) {
            return ctx.y;
        }

        String cleaned =
                clean(text);

        String[] words =
                cleaned.split("\\s+");

        StringBuilder line =
                new StringBuilder();

        for (String word : words) {

            String test =
                    line.length() == 0
                            ? word
                            : line + " " + word;

            float testWidth =
                    getTextWidth(
                            test,
                            font,
                            fontSize
                    );

            if (testWidth > width &&
                    line.length() > 0) {

                drawText(
                        cs,
                        line.toString(),
                        x,
                        ctx.y,
                        font,
                        fontSize,
                        DARK
                );

                ctx.y -= leading;

                line =
                        new StringBuilder(word);

            } else {

                line =
                        new StringBuilder(test);
            }
        }

        if (line.length() > 0) {

            drawText(
                    cs,
                    line.toString(),
                    x,
                    ctx.y,
                    font,
                    fontSize,
                    DARK
            );

            ctx.y -= leading;
        }

        return ctx.y;
    }

    // =========================================================
    // COLUMN DIVIDER
    // =========================================================

    private void drawColumnDivider(
            PDPageContentStream cs,
            float top
    ) throws IOException {

        float dividerX =
                LEFT +
                        LEFT_COLUMN_WIDTH +
                        (COLUMN_GAP / 2f);

        cs.setStrokingColor(
                LIGHT_GRAY
        );

        cs.setLineWidth(
                0.45f
        );

        cs.moveTo(
                dividerX,
                top - 2f
        );

        cs.lineTo(
                dividerX,
                BOTTOM
        );

        cs.stroke();
    }

    // =========================================================
    // DRAW TEXT
    // =========================================================

    private void drawText(
            PDPageContentStream cs,
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

        cs.beginText();

        cs.setFont(
                font,
                fontSize
        );

        cs.setNonStrokingColor(
                color
        );

        cs.newLineAtOffset(
                x,
                y
        );

        cs.showText(
                clean(text)
        );

        cs.endText();
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
    // LINK
    // =========================================================

    private void addLink(
            PDDocument document,
            PDPage page,
            String text,
            float x,
            float y,
            float width,
            float fontSize,
            String url
    ) throws IOException {

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
                url
        );

        link.setAction(
                action
        );

        page.getAnnotations()
                .add(link);
    }

    // =========================================================
    // URL
    // =========================================================

    private String normalizeUrl(
            String url
    ) {

        if (!hasText(url)) {
            return "";
        }

        if (url.startsWith("http://") ||
                url.startsWith("https://")) {

            return url;
        }

        return "https://" + url;
    }

    // =========================================================
    // CLEAN
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
                .replace("\u2013", "-")
                .replace("\u2014", "-")
                .replace("\u2018", "'")
                .replace("\u2019", "'")
                .replace("\u201C", "\"")
                .replace("\u201D", "\"")
                .replaceAll("\\s+", " ")
                .trim();
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

        return value != null &&
                !value.trim().isEmpty();
    }
}

