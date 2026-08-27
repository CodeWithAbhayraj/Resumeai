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

    private static final float LEFT_MARGIN = 42f;
    private static final float RIGHT_MARGIN = 42f;
    private static final float TOP_MARGIN = 32f;
    private static final float BOTTOM_MARGIN = 28f;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;

    // =========================================================
    // FONTS
    // =========================================================

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final PDType1Font FONT_ITALIC =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float NAME_SIZE = 20f;
    private static final float CONTACT_SIZE = 8.5f;

    private static final float SECTION_SIZE = 10.5f;

    private static final float BODY_SIZE = 8.5f;
    private static final float PROJECT_TITLE_SIZE = 9.5f;
    private static final float TECH_SIZE = 8f;

    private static final float EDUCATION_SIZE = 9f;
    private static final float SMALL_SIZE = 8.2f;

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BLACK = Color.BLACK;

    private static final Color DARK_GRAY =
            new Color(55, 55, 55);

    private static final Color BLUE =
            new Color(30, 70, 120);

    private static final Color LINE_GRAY =
            new Color(150, 150, 150);

    // =========================================================
    // SPACING
    // =========================================================

    private static final float NAME_BOTTOM = 5f;
    private static final float CONTACT_BOTTOM = 9f;

    private static final float SECTION_TOP = 7f;
    private static final float SECTION_BOTTOM = 5f;

    private static final float BODY_LEADING = 10.5f;
    private static final float BULLET_LEADING = 10.2f;

    private static final float BLOCK_GAP = 3f;

    // =========================================================
    // CONTEXT
    // =========================================================

    private static class Context {

        float y;

        Context(float y) {
            this.y = y;
        }
    }

    // =========================================================
    // MAIN METHOD
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

            Context ctx =
                    new Context(
                            PAGE_HEIGHT - TOP_MARGIN
                    );

            try (PDPageContentStream cs =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                // =================================================
                // HEADER
                // =================================================

                writeHeader(
                        document,
                        page,
                        cs,
                        ctx,
                        resume
                );

                // =================================================
                // SUMMARY
                // =================================================

                if (hasText(
                        resume.getProfessionalSummary()
                )) {

                    writeSection(
                            cs,
                            ctx,
                            "PROFESSIONAL SUMMARY"
                    );

                    writeWrappedText(
                            cs,
                            ctx,
                            resume.getProfessionalSummary(),
                            LEFT_MARGIN,
                            CONTENT_WIDTH,
                            BODY_SIZE,
                            FONT_REGULAR,
                            BODY_LEADING
                    );
                }

                // =================================================
                // EXPERIENCE
                // =================================================

                if (resume.getExperience() != null &&
                        !resume.getExperience().isEmpty()) {

                    writeSection(
                            cs,
                            ctx,
                            "EXPERIENCE"
                    );

                    for (ExperienceDTO experience :
                            resume.getExperience()) {

                        writeExperience(
                                cs,
                                ctx,
                                experience
                        );
                    }
                }

                // =================================================
                // PROJECTS
                // =================================================

                if (resume.getProjects() != null &&
                        !resume.getProjects().isEmpty()) {

                    writeSection(
                            cs,
                            ctx,
                            "PROJECTS"
                    );

                    for (ProjectDTO project :
                            resume.getProjects()) {

                        writeProject(
                                cs,
                                ctx,
                                project
                        );
                    }
                }

                // =================================================
                // EDUCATION
                // =================================================

                if (resume.getEducation() != null &&
                        !resume.getEducation().isEmpty()) {

                    writeSection(
                            cs,
                            ctx,
                            "EDUCATION"
                    );

                    for (EducationDTO education :
                            resume.getEducation()) {

                        writeEducation(
                                cs,
                                ctx,
                                education
                        );
                    }
                }

                // =================================================
                // CERTIFICATIONS
                // =================================================

                if (resume.getCertifications() != null &&
                        !resume.getCertifications().isEmpty()) {

                    writeSection(
                            cs,
                            ctx,
                            "CERTIFICATIONS"
                    );

                    for (String certification :
                            resume.getCertifications()) {

                        if (hasText(certification)) {

                            writeBullet(
                                    cs,
                                    ctx,
                                    certification
                            );
                        }
                    }
                }

                // =================================================
                // SKILLS
                // =================================================

                SkillsDTO skills =
                        resume.getSkills();

                if (skills != null) {

                    writeSection(
                            cs,
                            ctx,
                            "TECHNICAL SKILLS"
                    );

                    writeSkill(
                            cs,
                            ctx,
                            "Languages",
                            skills.getLanguages()
                    );

                    writeSkill(
                            cs,
                            ctx,
                            "Frameworks",
                            skills.getFrameworks()
                    );

                    writeSkill(
                            cs,
                            ctx,
                            "Databases",
                            skills.getDatabases()
                    );

                    writeSkill(
                            cs,
                            ctx,
                            "DevOps & Tools",
                            skills.getDevopsAndTools()
                    );

                    writeSkill(
                            cs,
                            ctx,
                            "Concepts",
                            skills.getConcepts()
                    );
                }

                // =================================================
                // SINGLE PAGE CHECK
                // =================================================

                if (ctx.y < BOTTOM_MARGIN) {

                    throw new RuntimeException(
                            "Resume exceeds one A4 page. " +
                                    "Reduce resume content."
                    );
                }
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
            Context ctx,
            ImprovedResumeDTO resume
    ) throws IOException {

        String name =
                clean(
                        safe(
                                resume.getFullName()
                        )
                );

        // ---------------------------------------------------------
        // NAME
        // ---------------------------------------------------------

        if (hasText(name)) {

            float width =
                    textWidth(
                            name,
                            FONT_BOLD,
                            NAME_SIZE
                    );

            float x =
                    (PAGE_WIDTH - width) / 2f;

            drawText(
                    cs,
                    name,
                    x,
                    ctx.y,
                    FONT_BOLD,
                    NAME_SIZE,
                    BLACK
            );

            ctx.y -=
                    NAME_SIZE + NAME_BOTTOM;
        }

        // ---------------------------------------------------------
        // CONTACT
        // ---------------------------------------------------------

        String email =
                clean(
                        safe(
                                resume.getEmail()
                        )
                );

        String linkedin =
                clean(
                        safe(
                                resume.getLinkedin()
                        )
                );

        String github =
                clean(
                        safe(
                                resume.getGithub()
                        )
                );

        String separator =
                "  |  ";

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

            float width =
                    textWidth(
                            contactText,
                            FONT_REGULAR,
                            CONTACT_SIZE
                    );

            float x =
                    (PAGE_WIDTH - width) / 2f;

            drawText(
                    cs,
                    contactText,
                    x,
                    ctx.y,
                    FONT_REGULAR,
                    CONTACT_SIZE,
                    DARK_GRAY
            );

            // Clickable links
            createLinks(
                    document,
                    page,
                    x,
                    ctx.y,
                    email,
                    linkedin,
                    github
            );

            ctx.y -=
                    CONTACT_SIZE +
                            CONTACT_BOTTOM;
        }

        // ---------------------------------------------------------
        // HEADER LINE
        // ---------------------------------------------------------

        cs.setStrokingColor(BLUE);
        cs.setLineWidth(1f);

        cs.moveTo(
                LEFT_MARGIN,
                ctx.y
        );

        cs.lineTo(
                PAGE_WIDTH - RIGHT_MARGIN,
                ctx.y
        );

        cs.stroke();

        ctx.y -= 3f;
    }

    // =========================================================
    // SECTION
    // =========================================================

    private void writeSection(
            PDPageContentStream cs,
            Context ctx,
            String title
    ) throws IOException {

        ctx.y -= SECTION_TOP;

        drawText(
                cs,
                title,
                LEFT_MARGIN,
                ctx.y,
                FONT_BOLD,
                SECTION_SIZE,
                BLUE
        );

        float titleWidth =
                textWidth(
                        title,
                        FONT_BOLD,
                        SECTION_SIZE
                );

        float lineY =
                ctx.y - 2.5f;

        cs.setStrokingColor(
                LINE_GRAY
        );

        cs.setLineWidth(
                0.45f
        );

        cs.moveTo(
                LEFT_MARGIN + titleWidth + 7f,
                lineY
        );

        cs.lineTo(
                PAGE_WIDTH - RIGHT_MARGIN,
                lineY
        );

        cs.stroke();

        ctx.y -= SECTION_BOTTOM;
    }

    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperience(
            PDPageContentStream cs,
            Context ctx,
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
                                " - " +
                                company;

            } else {

                heading = company;
            }
        }

        // Heading
        if (hasText(heading)) {

            drawText(
                    cs,
                    heading,
                    LEFT_MARGIN,
                    ctx.y,
                    FONT_BOLD,
                    PROJECT_TITLE_SIZE,
                    BLACK
            );

            ctx.y -= 11f;
        }

        // Duration
        if (hasText(
                experience.getDuration()
        )) {

            drawText(
                    cs,
                    clean(
                            experience.getDuration()
                    ),
                    LEFT_MARGIN,
                    ctx.y,
                    FONT_ITALIC,
                    SMALL_SIZE,
                    DARK_GRAY
            );

            ctx.y -= 10f;
        }

        // Description
        if (hasText(
                experience.getDescription()
        )) {

            ctx.y =
                    writeBullet(
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

    private void writeProject(
            PDPageContentStream cs,
            Context ctx,
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

        // ---------------------------------------------------------
        // PROJECT TITLE
        // ---------------------------------------------------------

        if (hasText(title)) {

            drawText(
                    cs,
                    title,
                    LEFT_MARGIN,
                    ctx.y,
                    FONT_BOLD,
                    PROJECT_TITLE_SIZE,
                    BLACK
            );

            ctx.y -= 11f;
        }

        // ---------------------------------------------------------
        // TECHNOLOGIES
        // ---------------------------------------------------------

        if (project.getTechnologies() != null &&
                !project.getTechnologies().isEmpty()) {

            String tech =
                    clean(
                            String.join(
                                    " | ",
                                    project.getTechnologies()
                            )
                    );

            if (hasText(tech)) {

                ctx.y =
                        writeWrappedText(
                                cs,
                                ctx,
                                "Technologies: " + tech,
                                LEFT_MARGIN,
                                CONTENT_WIDTH,
                                TECH_SIZE,
                                FONT_ITALIC,
                                9.5f
                        );
            }
        }

        // ---------------------------------------------------------
        // HIGHLIGHTS
        // ---------------------------------------------------------

        if (project.getHighlights() != null) {

            for (String highlight :
                    project.getHighlights()) {

                if (hasText(highlight)) {

                    ctx.y =
                            writeBullet(
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
    // EDUCATION
    // =========================================================

    private void writeEducation(
            PDPageContentStream cs,
            Context ctx,
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

        // Institution
        if (hasText(institution)) {

            drawText(
                    cs,
                    institution,
                    LEFT_MARGIN,
                    ctx.y,
                    FONT_BOLD,
                    EDUCATION_SIZE,
                    BLACK
            );

            ctx.y -= 11f;
        }

        // Degree
        if (hasText(degree)) {

            ctx.y =
                    writeWrappedText(
                            cs,
                            ctx,
                            degree,
                            LEFT_MARGIN,
                            CONTENT_WIDTH,
                            SMALL_SIZE,
                            FONT_REGULAR,
                            9.5f
                    );
        }

        // Dates
        String dates = "";

        if (hasText(start) &&
                hasText(end)) {

            dates =
                    start +
                            " - " +
                            end;

        } else if (hasText(start)) {

            dates = start;

        } else if (hasText(end)) {

            dates = end;
        }

        if (hasText(dates)) {

            drawText(
                    cs,
                    dates,
                    LEFT_MARGIN,
                    ctx.y,
                    FONT_ITALIC,
                    SMALL_SIZE,
                    DARK_GRAY
            );

            ctx.y -= 10f;
        }

        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // CERTIFICATION / BULLET
    // =========================================================

    private float writeBullet(
            PDPageContentStream cs,
            Context ctx,
            String text
    ) throws IOException {

        if (!hasText(text)) {
            return ctx.y;
        }

        float bulletX =
                LEFT_MARGIN;

        float textX =
                LEFT_MARGIN + 10f;

        drawText(
                cs,
                "•",
                bulletX,
                ctx.y,
                FONT_REGULAR,
                BODY_SIZE,
                BLACK
        );

        return writeWrappedText(
                cs,
                ctx,
                text,
                textX,
                CONTENT_WIDTH - 10f,
                BODY_SIZE,
                FONT_REGULAR,
                BULLET_LEADING
        );
    }

    // =========================================================
    // SKILLS
    // =========================================================

    private void writeSkill(
            PDPageContentStream cs,
            Context ctx,
            String label,
            List<String> values
    ) throws IOException {

        if (values == null ||
                values.isEmpty()) {
            return;
        }

        String skills =
                clean(
                        String.join(
                                ", ",
                                values
                        )
                );

        if (!hasText(skills)) {
            return;
        }

        String complete =
                label +
                        ": " +
                        skills;

        ctx.y =
                writeWrappedText(
                        cs,
                        ctx,
                        complete,
                        LEFT_MARGIN,
                        CONTENT_WIDTH,
                        SMALL_SIZE,
                        FONT_REGULAR,
                        9.5f
                );
    }

    // =========================================================
    // WRAP
    // =========================================================

    private float writeWrappedText(
            PDPageContentStream cs,
            Context ctx,
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

            String candidate =
                    line.length() == 0
                            ? word
                            : line + " " + word;

            float candidateWidth =
                    textWidth(
                            candidate,
                            font,
                            fontSize
                    );

            if (candidateWidth > width &&
                    line.length() > 0) {

                drawText(
                        cs,
                        line.toString(),
                        x,
                        ctx.y,
                        font,
                        fontSize,
                        DARK_GRAY
                );

                ctx.y -= leading;

                line =
                        new StringBuilder(word);

            } else {

                line =
                        new StringBuilder(candidate);
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
                    DARK_GRAY
            );

            ctx.y -= leading;
        }

        return ctx.y;
    }

    // =========================================================
    // CLICKABLE LINKS
    // =========================================================

    private void createLinks(
            PDDocument document,
            PDPage page,
            float x,
            float y,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        String separator =
                "  |  ";

        float currentX = x;

        if (hasText(email)) {

            float width =
                    textWidth(
                            email,
                            FONT_REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    "mailto:" + email
            );

            currentX += width;
        }

        if (hasText(linkedin)) {

            if (currentX > x) {

                currentX +=
                        textWidth(
                                separator,
                                FONT_REGULAR,
                                CONTACT_SIZE
                        );
            }

            float width =
                    textWidth(
                            linkedin,
                            FONT_REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    normalizeUrl(linkedin)
            );

            currentX += width;
        }

        if (hasText(github)) {

            if (currentX > x) {

                currentX +=
                        textWidth(
                                separator,
                                FONT_REGULAR,
                                CONTACT_SIZE
                        );
            }

            float width =
                    textWidth(
                            github,
                            FONT_REGULAR,
                            CONTACT_SIZE
                    );

            addLink(
                    document,
                    page,
                    currentX,
                    y,
                    width,
                    CONTACT_SIZE,
                    normalizeUrl(github)
            );
        }
    }

    // =========================================================
    // ADD LINK
    // =========================================================

    private void addLink(
            PDDocument document,
            PDPage page,
            float x,
            float y,
            float width,
            float height,
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
        rectangle.setLowerLeftY(y - 2f);

        rectangle.setUpperRightX(
                x + width
        );

        rectangle.setUpperRightY(
                y + height + 2f
        );

        link.setRectangle(rectangle);

        link.setHighlightMode(
                PDAnnotationLink.HIGHLIGHT_MODE_NONE
        );

        PDActionURI action =
                new PDActionURI();

        action.setURI(url);

        link.setAction(action);

        page.getAnnotations().add(link);
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
            float size,
            Color color
    ) throws IOException {

        if (!hasText(text)) {
            return;
        }

        cs.beginText();

        cs.setFont(
                font,
                size
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

    private float textWidth(
            String text,
            PDType1Font font,
            float size
    ) {

        if (!hasText(text)) {
            return 0f;
        }

        try {

            return font.getStringWidth(
                    clean(text)
            ) / 1000f * size;

        } catch (IOException e) {

            return 0f;
        }
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