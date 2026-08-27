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
    private static final float TOP_MARGIN = 36f;
    private static final float BOTTOM_MARGIN = 30f;

    // Full usable width for single-column layout
    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;

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
    private static final Color DARK = new Color(45, 45, 45);
    private static final Color GRAY = new Color(95, 95, 95);
    private static final Color LIGHT_GRAY = new Color(180, 180, 180);
    private static final Color ACCENT = new Color(30, 70, 120);

    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float NAME_SIZE = 24f;
    private static final float CONTACT_SIZE = 9f;

    private static final float SECTION_TITLE_SIZE = 11f;
    private static final float SUMMARY_SIZE = 9f;
    private static final float BODY_SIZE = 8.8f;
    private static final float SUBTITLE_SIZE = 9.2f;
    private static final float SKILL_SIZE = 8.5f;
    private static final float BULLET_SIZE = 8.5f;

    // =========================================================
    // SPACING
    // =========================================================

    private static final float HEADER_BOTTOM = 12f;
    private static final float SECTION_TOP = 9f;
    private static final float SECTION_BOTTOM = 6f;
    private static final float BLOCK_GAP = 5f;
    private static final float BODY_LEADING = 11f;
    private static final float BULLET_GAP = 2f;

    // =========================================================
    // PAGE CONTEXT
    // =========================================================

    private static class Context {
        float y;
        Context(float y) { this.y = y; }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public byte[] generateResumePdf(ImprovedResumeDTO resume) {
        if (resume == null) {
            throw new IllegalArgumentException("Resume data cannot be null.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float startY = PAGE_HEIGHT - TOP_MARGIN;
                Context ctx = new Context(startY);

                // ---- Header ----
                float headerEndY = writeHeader(document, page, cs, resume, ctx);
                ctx.y = headerEndY - HEADER_BOTTOM;

                // ---- Summary ----
                if (hasText(resume.getProfessionalSummary())) {
                    writeSectionTitle(cs, ctx, "PROFESSIONAL SUMMARY");
                    ctx.y = writeWrapped(cs, ctx, resume.getProfessionalSummary(),
                            LEFT_MARGIN, CONTENT_WIDTH, SUMMARY_SIZE, REGULAR, BODY_LEADING);
                    ctx.y -= BLOCK_GAP;
                }

                // ---- Skills ----
                SkillsDTO skills = resume.getSkills();
                if (skills != null) {
                    writeSectionTitle(cs, ctx, "TECHNICAL SKILLS");
                    writeSkillsBlock(cs, ctx, skills);
                    ctx.y -= BLOCK_GAP;
                }

                // ---- Experience ----
                if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
                    writeSectionTitle(cs, ctx, "EXPERIENCE");
                    for (ExperienceDTO exp : resume.getExperience()) {
                        writeExperienceBlock(cs, ctx, exp);
                    }
                }

                // ---- Projects ----
                if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
                    writeSectionTitle(cs, ctx, "PROJECTS");
                    for (ProjectDTO project : resume.getProjects()) {
                        writeProjectBlock(cs, ctx, project);
                    }
                }

                // ---- Education ----
                if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                    writeSectionTitle(cs, ctx, "EDUCATION");
                    for (EducationDTO edu : resume.getEducation()) {
                        writeEducationBlock(cs, ctx, edu);
                    }
                }

                // ---- Certifications ----
                if (resume.getCertifications() != null && !resume.getCertifications().isEmpty()) {
                    writeSectionTitle(cs, ctx, "CERTIFICATIONS");
                    for (String cert : resume.getCertifications()) {
                        if (hasText(cert)) {
                            writeBullet(cs, ctx, cert);
                        }
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Unable to generate resume PDF.", e);
        }
    }

    // =========================================================
    // HEADER
    // =========================================================

    private float writeHeader(PDDocument document, PDPage page, PDPageContentStream cs,
                              ImprovedResumeDTO resume, Context ctx) throws IOException {
        float y = ctx.y;

        // Name
        String name = clean(safe(resume.getFullName()));
        if (hasText(name)) {
            float nameWidth = getTextWidth(name, BOLD, NAME_SIZE);
            float nameX = (PAGE_WIDTH - nameWidth) / 2f;
            drawText(cs, name, nameX, y, BOLD, NAME_SIZE, BLACK);
            y -= 20f;
        }

        // Contact info: email, LinkedIn, GitHub
        String email = clean(safe(resume.getEmail()));
        String linkedin = clean(safe(resume.getLinkedin()));
        String github = clean(safe(resume.getGithub()));
        String sep = "  |  ";
        StringBuilder contact = new StringBuilder();
        if (hasText(email)) contact.append(email);
        if (hasText(linkedin)) {
            if (contact.length() > 0) contact.append(sep);
            contact.append(linkedin);
        }
        if (hasText(github)) {
            if (contact.length() > 0) contact.append(sep);
            contact.append(github);
        }
        String contactText = contact.toString();
        if (hasText(contactText)) {
            float contactWidth = getTextWidth(contactText, REGULAR, CONTACT_SIZE);
            float contactX = (PAGE_WIDTH - contactWidth) / 2f;
            drawText(cs, contactText, contactX, y, REGULAR, CONTACT_SIZE, GRAY);
            createContactLinks(document, page, contactText, contactX, y, email, linkedin, github);
            y -= 14f;
        }

        // Horizontal line below header
        cs.setStrokingColor(ACCENT);
        cs.setLineWidth(1.2f);
        cs.moveTo(LEFT_MARGIN, y);
        cs.lineTo(PAGE_WIDTH - RIGHT_MARGIN, y);
        cs.stroke();

        return y;
    }

    private void createContactLinks(PDDocument document, PDPage page, String fullText,
                                    float startX, float y, String email, String linkedin, String github)
            throws IOException {
        String separator = "  |  ";
        float currentX = startX;

        if (hasText(email)) {
            float w = getTextWidth(email, REGULAR, CONTACT_SIZE);
            addLink(document, page, email, currentX, y, w, CONTACT_SIZE, "mailto:" + email);
            currentX += w;
        }
        if (hasText(linkedin)) {
            if (currentX > startX) currentX += getTextWidth(separator, REGULAR, CONTACT_SIZE);
            float w = getTextWidth(linkedin, REGULAR, CONTACT_SIZE);
            addLink(document, page, linkedin, currentX, y, w, CONTACT_SIZE, normalizeUrl(linkedin));
            currentX += w;
        }
        if (hasText(github)) {
            if (currentX > startX) currentX += getTextWidth(separator, REGULAR, CONTACT_SIZE);
            float w = getTextWidth(github, REGULAR, CONTACT_SIZE);
            addLink(document, page, github, currentX, y, w, CONTACT_SIZE, normalizeUrl(github));
        }
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private void writeSectionTitle(PDPageContentStream cs, Context ctx, String title)
            throws IOException {
        ctx.y -= SECTION_TOP;
        drawText(cs, title, LEFT_MARGIN, ctx.y, BOLD, SECTION_TITLE_SIZE, ACCENT);

        float titleWidth = getTextWidth(title, BOLD, SECTION_TITLE_SIZE);
        float lineY = ctx.y - 3f;
        cs.setStrokingColor(LIGHT_GRAY);
        cs.setLineWidth(0.6f);
        cs.moveTo(LEFT_MARGIN + titleWidth + 8f, lineY);
        cs.lineTo(PAGE_WIDTH - RIGHT_MARGIN, lineY);
        cs.stroke();

        ctx.y -= SECTION_BOTTOM;
    }

    // =========================================================
    // SKILLS
    // =========================================================

    private void writeSkillsBlock(PDPageContentStream cs, Context ctx, SkillsDTO skills)
            throws IOException {
        if (skills == null) return;

        writeSkillLine(cs, ctx, "Languages", skills.getLanguages());
        writeSkillLine(cs, ctx, "Frameworks", skills.getFrameworks());
        writeSkillLine(cs, ctx, "Databases", skills.getDatabases());
        writeSkillLine(cs, ctx, "DevOps & Tools", skills.getDevopsAndTools());
        writeSkillLine(cs, ctx, "Concepts", skills.getConcepts());
    }

    private void writeSkillLine(PDPageContentStream cs, Context ctx,
                                String label, List<String> values) throws IOException {
        if (values == null || values.isEmpty()) return;
        String text = clean(String.join(", ", values));
        if (!hasText(text)) return;

        String line = label + ": " + text;
        ctx.y = writeWrapped(cs, ctx, line, LEFT_MARGIN, CONTENT_WIDTH,
                SKILL_SIZE, REGULAR, 10f);
        ctx.y -= 3f;  // small gap between skill lines
    }

    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperienceBlock(PDPageContentStream cs, Context ctx,
                                      ExperienceDTO exp) throws IOException {
        String role = clean(safe(exp.getRole()));
        String company = clean(safe(exp.getCompany()));
        String heading = role;
        if (hasText(company)) {
            heading = hasText(heading) ? heading + " | " + company : company;
        }
        if (hasText(heading)) {
            drawText(cs, heading, LEFT_MARGIN, ctx.y, BOLD, SUBTITLE_SIZE, DARK);
            ctx.y -= 11f;
        }
        if (hasText(exp.getDuration())) {
            ctx.y = writeWrapped(cs, ctx, clean(exp.getDuration()),
                    LEFT_MARGIN, CONTENT_WIDTH, BODY_SIZE, ITALIC, 10f);
        }
        if (hasText(exp.getDescription())) {
            ctx.y = writeBullet(cs, ctx, exp.getDescription());
        }
        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // PROJECT
    // =========================================================

    private void writeProjectBlock(PDPageContentStream cs, Context ctx,
                                   ProjectDTO project) throws IOException {
        String title = clean(safe(project.getTitle()));
        if (hasText(title)) {
            drawText(cs, title, LEFT_MARGIN, ctx.y, BOLD, SUBTITLE_SIZE, DARK);
            ctx.y -= 11f;
        }
        if (project.getTechnologies() != null && !project.getTechnologies().isEmpty()) {
            String tech = clean(String.join(" • ", project.getTechnologies()));
            ctx.y = writeWrapped(cs, ctx, tech, LEFT_MARGIN, CONTENT_WIDTH,
                    BODY_SIZE, ITALIC, 10f);
        }
        if (project.getHighlights() != null) {
            for (String highlight : project.getHighlights()) {
                if (hasText(highlight)) {
                    ctx.y = writeBullet(cs, ctx, highlight);
                }
            }
        }
        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // EDUCATION
    // =========================================================

    private void writeEducationBlock(PDPageContentStream cs, Context ctx,
                                     EducationDTO edu) throws IOException {
        String institution = clean(safe(edu.getInstitution()));
        String degree = clean(safe(edu.getDegree()));
        String start = clean(safe(edu.getStartDate()));
        String end = clean(safe(edu.getEndDate()));

        if (hasText(institution)) {
            drawText(cs, institution, LEFT_MARGIN, ctx.y, BOLD, SUBTITLE_SIZE, DARK);
            ctx.y -= 11f;
        }
        if (hasText(degree)) {
            ctx.y = writeWrapped(cs, ctx, degree, LEFT_MARGIN, CONTENT_WIDTH,
                    BODY_SIZE, REGULAR, 10f);
        }
        if (hasText(start) || hasText(end)) {
            String dates = start;
            if (hasText(start) && hasText(end)) {
                dates = start + " - " + end;
            } else if (hasText(end)) {
                dates = end;
            }
            ctx.y = writeWrapped(cs, ctx, dates, LEFT_MARGIN, CONTENT_WIDTH,
                    BODY_SIZE, ITALIC, 10f);
            ctx.y -= 2f;
        }
        ctx.y -= BLOCK_GAP;
    }

    // =========================================================
    // BULLET
    // =========================================================

    private float writeBullet(PDPageContentStream cs, Context ctx, String text)
            throws IOException {
        float bulletX = LEFT_MARGIN;
        float textX = LEFT_MARGIN + 12f;
        float bulletWidth = CONTENT_WIDTH - 12f;

        drawText(cs, "•", bulletX, ctx.y, REGULAR, BULLET_SIZE, DARK);
        ctx.y = writeWrapped(cs, ctx, text, textX, bulletWidth,
                BULLET_SIZE, REGULAR, BODY_LEADING);
        ctx.y -= BULLET_GAP;
        return ctx.y;
    }

    // =========================================================
    // WRAPPED TEXT
    // =========================================================

    private float writeWrapped(PDPageContentStream cs, Context ctx,
                               String text, float x, float width, float fontSize,
                               PDType1Font font, float leading) throws IOException {
        if (!hasText(text)) return ctx.y;

        String cleaned = clean(text);
        String[] words = cleaned.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (getTextWidth(test, font, fontSize) > width && line.length() > 0) {
                drawText(cs, line.toString(), x, ctx.y, font, fontSize, DARK);
                ctx.y -= leading;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            drawText(cs, line.toString(), x, ctx.y, font, fontSize, DARK);
            ctx.y -= leading;
        }
        return ctx.y;
    }

    // =========================================================
    // DRAW TEXT & WIDTH
    // =========================================================

    private void drawText(PDPageContentStream cs, String text, float x, float y,
                          PDType1Font font, float size, Color color) throws IOException {
        if (!hasText(text)) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(clean(text));
        cs.endText();
    }

    private float getTextWidth(String text, PDType1Font font, float size) {
        if (!hasText(text)) return 0f;
        try {
            return font.getStringWidth(clean(text)) / 1000f * size;
        } catch (IOException e) {
            return 0f;
        }
    }

    // =========================================================
    // LINK
    // =========================================================

    private void addLink(PDDocument document, PDPage page, String text,
                         float x, float y, float width, float fontSize, String url)
            throws IOException {
        if (!hasText(url)) return;
        PDAnnotationLink link = new PDAnnotationLink();
        PDRectangle rect = new PDRectangle();
        rect.setLowerLeftX(x);
        rect.setLowerLeftY(y - 2f);
        rect.setUpperRightX(x + width);
        rect.setUpperRightY(y + fontSize + 2f);
        link.setRectangle(rect);
        link.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_NONE);
        PDActionURI action = new PDActionURI();
        action.setURI(url);
        link.setAction(action);
        page.getAnnotations().add(link);
    }

    private String normalizeUrl(String url) {
        if (!hasText(url)) return "";
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return "https://" + url;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String clean(String value) {
        if (value == null) return "";
        return value.replace("\r", " ").replace("\n", " ")
                .replace("\u2013", "-").replace("\u2014", "-")
                .replace("\u2018", "'").replace("\u2019", "'")
                .replace("\u201C", "\"").replace("\u201D", "\"")
                .replaceAll("\\s+", " ").trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}