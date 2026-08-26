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

    private static final PDRectangle PAGE_SIZE  = PDRectangle.A4;
    private static final float PAGE_WIDTH       = PAGE_SIZE.getWidth();
    private static final float PAGE_HEIGHT      = PAGE_SIZE.getHeight();

    private static final float LEFT_MARGIN      = 52f;
    private static final float RIGHT_MARGIN     = 52f;
    private static final float TOP_MARGIN       = 38f;
    private static final float BOTTOM_MARGIN    = 30f;

    private static final float CONTENT_WIDTH    = PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;


    // =========================================================
    // FONTS
    // =========================================================

    private static final PDType1Font REGULAR =
            new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

    private static final PDType1Font BOLD =
            new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);

    private static final PDType1Font ITALIC =
            new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);

    private static final PDType1Font BOLD_ITALIC =
            new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC);


    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float NAME_SIZE                = 20f;   // was 18f — matches image 2
    private static final float CONTACT_SIZE             = 9f;    // was 8.5f
    private static final float SECTION_SIZE             = 11f;   // was 10.5f
    private static final float SUMMARY_SIZE             = 9.5f;  // was 9f
    private static final float PROJECT_TITLE_SIZE       = 10.5f; // was 10f
    private static final float PROJECT_TECH_SIZE        = 9f;    // was 8.8f
    private static final float BULLET_SIZE              = 9f;    // was 8.7f
    private static final float EDUCATION_INST_SIZE      = 10f;   // was 9.5f
    private static final float EDUCATION_DETAIL_SIZE    = 9f;    // was 8.7f
    private static final float DATE_SIZE                = 9f;    // was 8.3f — align with inst size
    private static final float CERTIFICATION_SIZE       = 9f;    // was 8.7f
    private static final float SKILLS_SIZE              = 9f;    // was 8.5f


    // =========================================================
    // SPACING — tweak here to breathe or compress
    // =========================================================

    /** Gap between name and contact line */
    private static final float AFTER_NAME           = 16f;

    /** Gap after contact line before first section */
    private static final float AFTER_CONTACT        = 10f;

    /** Drop inside writeSectionTitle before drawing title */
    private static final float BEFORE_SECTION       = 4f;

    /** Drop after section title line (title height + rule gap) */
    private static final float AFTER_SECTION_TITLE  = 13f;

    /** Extra gap appended after each project/education/experience block */
    private static final float AFTER_BLOCK          = 3f;

    /** Line-height multiplier for summary text */
    private static final float SUMMARY_LEADING      = 2.5f;

    /** Line-height multiplier for bullet text */
    private static final float BULLET_LEADING       = 2.2f;

    /** Line-height multiplier for skill value text */
    private static final float SKILL_LEADING        = 1.8f;

    /** Gap after each skill row */
    private static final float AFTER_SKILL_ROW      = 1f;

    /** Bullet indent from LEFT_MARGIN */
    private static final float BULLET_CHAR_X        = LEFT_MARGIN + 8f;

    /** Bullet text indent from LEFT_MARGIN */
    private static final float BULLET_TEXT_X        = LEFT_MARGIN + 18f;


    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BLACK     = Color.BLACK;
    private static final Color LINK_BLUE = new Color(0, 70, 180);


    // =========================================================
    // PAGE CONTEXT  (keeps yPosition off the singleton)
    // =========================================================

    private static class PageContext {
        float y;
        PageContext(float y) { this.y = y; }
    }


    // =========================================================
    // MAIN ENTRY
    // =========================================================

    public byte[] generateResumePdf(ImprovedResumeDTO resume) {

        if (resume == null) {
            throw new IllegalArgumentException("Resume data cannot be null.");
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            PageContext ctx = new PageContext(PAGE_HEIGHT - TOP_MARGIN);

            try (PDPageContentStream cs =
                         new PDPageContentStream(document, page)) {

                // 1. NAME
                writeName(cs, ctx, resume.getFullName());

                // 2. CONTACT
                writeContactInfo(document, page, cs, ctx,
                        resume.getEmail(),
                        resume.getLinkedin(),
                        resume.getGithub());

                ctx.y -= AFTER_CONTACT;

                // 3. SUMMARY
                if (hasText(resume.getProfessionalSummary())) {
                    writeSectionTitle(cs, ctx, "Summary");
                    writeWrappedText(cs, ctx,
                            resume.getProfessionalSummary(),
                            LEFT_MARGIN, SUMMARY_SIZE, REGULAR, SUMMARY_LEADING);
                    ctx.y -= AFTER_BLOCK;
                }

                // 4. PROJECTS
                if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
                    writeSectionTitle(cs, ctx, "Projects");
                    for (ProjectDTO p : resume.getProjects()) {
                        writeProject(cs, ctx, p);
                        ctx.y -= AFTER_BLOCK;
                    }
                }

                // 5. EDUCATION
                if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                    writeSectionTitle(cs, ctx, "Education");
                    for (EducationDTO e : resume.getEducation()) {
                        writeEducation(cs, ctx, e);
                        ctx.y -= AFTER_BLOCK;
                    }
                }

                // 6. EXPERIENCE
                if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
                    writeSectionTitle(cs, ctx, "Experience");
                    for (ExperienceDTO e : resume.getExperience()) {
                        writeExperience(cs, ctx, e);
                        ctx.y -= AFTER_BLOCK;
                    }
                }

                // 7. CERTIFICATIONS
                if (resume.getCertifications() != null
                        && !resume.getCertifications().isEmpty()) {
                    writeSectionTitle(cs, ctx, "Certifications");
                    for (String cert : resume.getCertifications()) {
                        if (hasText(cert)) {
                            writeCertificationBullet(cs, ctx, cert);
                        }
                    }
                    ctx.y -= AFTER_BLOCK;
                }

                // 8. TECHNICAL SKILLS
                SkillsDTO skills = resume.getSkills();
                if (skills != null) {
                    writeSectionTitle(cs, ctx, "Technical Skills");
                    writeSkillLine(cs, ctx, "Languages:",    skills.getLanguages());
                    writeSkillLine(cs, ctx, "Frameworks:",   skills.getFrameworks());
                    writeSkillLine(cs, ctx, "Databases:",    skills.getDatabases());
                    writeSkillLine(cs, ctx, "DevOps & Tools:", skills.getDevopsAndTools());
                    writeSkillLine(cs, ctx, "Concepts:",     skills.getConcepts());
                }

                // PAGE OVERFLOW GUARD
                if (ctx.y < BOTTOM_MARGIN) {
                    throw new RuntimeException(
                            "Resume content exceeds one A4 page. " +
                                    "Please reduce resume content.");
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
    // NAME
    // =========================================================

    private void writeName(
            PDPageContentStream cs,
            PageContext ctx,
            String name
    ) throws IOException {

        if (!hasText(name)) return;

        String text  = clean(name);
        float  width = getTextWidth(text, BOLD, NAME_SIZE);
        float  x     = (PAGE_WIDTH - width) / 2f;

        drawText(cs, text, x, ctx.y, BOLD, NAME_SIZE, BLACK);
        ctx.y -= AFTER_NAME;
    }


    // =========================================================
    // CONTACT INFO
    // =========================================================

    private void writeContactInfo(
            PDDocument document,
            PDPage page,
            PDPageContentStream cs,
            PageContext ctx,
            String email,
            String linkedin,
            String github
    ) throws IOException {

        String emailText    = clean(safe(email));
        String linkedinText = clean(safe(linkedin));
        String githubText   = clean(safe(github));
        String sep          = "   \u2014   ";   // em-dash separator matching image 2

        int count = 0;
        if (hasText(emailText))    count++;
        if (hasText(linkedinText)) count++;
        if (hasText(githubText))   count++;
        if (count == 0) return;

        float emailW    = getTextWidth(emailText,    REGULAR, CONTACT_SIZE);
        float linkedinW = getTextWidth(linkedinText, REGULAR, CONTACT_SIZE);
        float githubW   = getTextWidth(githubText,   REGULAR, CONTACT_SIZE);
        float sepW      = getTextWidth(sep,          REGULAR, CONTACT_SIZE);

        float totalW = emailW + linkedinW + githubW + ((count - 1) * sepW);
        float x      = (PAGE_WIDTH - totalW) / 2f;

        // EMAIL
        if (hasText(emailText)) {
            drawContactLink(document, page, cs,
                    emailText, x, ctx.y, emailW, CONTACT_SIZE, null);
            x += emailW;
        }

        // LINKEDIN
        if (hasText(linkedinText)) {
            if (hasText(emailText)) {
                drawText(cs, sep, x, ctx.y, REGULAR, CONTACT_SIZE, BLACK);
                x += sepW;
            }
            drawContactLink(document, page, cs,
                    linkedinText, x, ctx.y, linkedinW, CONTACT_SIZE, linkedinText);
            x += linkedinW;
        }

        // GITHUB
        if (hasText(githubText)) {
            if (hasText(emailText) || hasText(linkedinText)) {
                drawText(cs, sep, x, ctx.y, REGULAR, CONTACT_SIZE, BLACK);
                x += sepW;
            }
            drawContactLink(document, page, cs,
                    githubText, x, ctx.y, githubW, CONTACT_SIZE, githubText);
        }

        ctx.y -= 13f;
    }


    // =========================================================
    // CLICKABLE LINK
    // =========================================================

    private void drawContactLink(
            PDDocument document,
            PDPage page,
            PDPageContentStream cs,
            String text, float x, float y,
            float width, float fontSize, String url
    ) throws IOException {

        drawText(cs, text, x, y, REGULAR, fontSize, LINK_BLUE);

        if (!hasText(url)) return;

        PDAnnotationLink link   = new PDAnnotationLink();
        PDRectangle      rect   = new PDRectangle();
        rect.setLowerLeftX(x);
        rect.setLowerLeftY(y - 2f);
        rect.setUpperRightX(x + width);
        rect.setUpperRightY(y + fontSize + 2f);
        link.setRectangle(rect);
        link.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_NONE);

        PDActionURI action = new PDActionURI();
        action.setURI(url.startsWith("http://") || url.startsWith("https://")
                ? url : "https://" + url);
        link.setAction(action);
        page.getAnnotations().add(link);
    }


    // =========================================================
    // SECTION TITLE  (bold text + full-width rule)
    // =========================================================

    private void writeSectionTitle(
            PDPageContentStream cs,
            PageContext ctx,
            String title
    ) throws IOException {

        if (!hasText(title)) return;

        ctx.y -= BEFORE_SECTION;
        ensureSpace(ctx, 18f);

        float titleWidth = getTextWidth(title, BOLD, SECTION_SIZE);

        drawText(cs, title, LEFT_MARGIN, ctx.y, BOLD, SECTION_SIZE, BLACK);

        // Horizontal rule starting just after the title text
        float lineY = ctx.y - 2.5f;
        cs.setStrokingColor(BLACK);
        cs.setLineWidth(0.5f);
        cs.moveTo(LEFT_MARGIN + titleWidth + 6f, lineY);
        cs.lineTo(PAGE_WIDTH - RIGHT_MARGIN, lineY);
        cs.stroke();

        ctx.y -= AFTER_SECTION_TITLE;
    }


    // =========================================================
    // PROJECT
    // =========================================================

    private void writeProject(
            PDPageContentStream cs,
            PageContext ctx,
            ProjectDTO project
    ) throws IOException {

        if (project == null) return;
        ensureSpace(ctx, 14f);

        String title = clean(safe(project.getTitle()));

        // Build technologies string
        String technologies = "";
        if (project.getTechnologies() != null
                && !project.getTechnologies().isEmpty()) {
            technologies = clean(String.join(" | ", project.getTechnologies()));
        }

        float titleWidth = getTextWidth(title, BOLD, PROJECT_TITLE_SIZE);
        float techWidth  = getTextWidth(technologies, ITALIC, PROJECT_TECH_SIZE);

        // ── Project header: title LEFT, tech stack RIGHT ──────────────────
        if (hasText(technologies)
                && titleWidth + techWidth + 25f <= CONTENT_WIDTH) {

            drawText(cs, title,
                    LEFT_MARGIN, ctx.y, BOLD, PROJECT_TITLE_SIZE, BLACK);

            float techX = PAGE_WIDTH - RIGHT_MARGIN - techWidth;
            drawText(cs, technologies,
                    techX, ctx.y, ITALIC, PROJECT_TECH_SIZE, BLACK);

            ctx.y -= (PROJECT_TITLE_SIZE + 3f);

        } else {
            drawText(cs, title,
                    LEFT_MARGIN, ctx.y, BOLD, PROJECT_TITLE_SIZE, BLACK);
            ctx.y -= (PROJECT_TITLE_SIZE + 3f);

            if (hasText(technologies)) {
                writeWrappedText(cs, ctx, technologies,
                        LEFT_MARGIN, PROJECT_TECH_SIZE, ITALIC, 1.5f);
            }
        }

        // ── Bullet points ─────────────────────────────────────────────────
        if (project.getHighlights() != null) {
            for (String highlight : project.getHighlights()) {
                if (hasText(highlight)) {
                    writeBullet(cs, ctx, highlight, BULLET_SIZE);
                }
            }
        }
    }


    // =========================================================
    // EDUCATION
    // =========================================================

    private void writeEducation(
            PDPageContentStream cs,
            PageContext ctx,
            EducationDTO education
    ) throws IOException {

        if (education == null) return;
        ensureSpace(ctx, 22f);

        String institution = clean(safe(education.getInstitution()));
        String degree      = clean(safe(education.getDegree()));
        String startDate   = clean(safe(education.getStartDate()));
        String endDate     = clean(safe(education.getEndDate()));

        String dates = "";
        if      (hasText(startDate) && hasText(endDate)) dates = startDate + " \u2013 " + endDate;
        else if (hasText(startDate))                     dates = startDate;
        else if (hasText(endDate))                       dates = endDate;

        // ── Row 1: Institution (bold) + dates (italic, right-aligned) ─────
        float dateWidth = getTextWidth(dates, ITALIC, DATE_SIZE);

        drawText(cs, institution,
                LEFT_MARGIN, ctx.y, BOLD, EDUCATION_INST_SIZE, BLACK);

        if (hasText(dates)) {
            float dateX = PAGE_WIDTH - RIGHT_MARGIN - dateWidth;
            // Vertically align date with the institution text baseline
            drawText(cs, dates,
                    dateX, ctx.y, ITALIC, DATE_SIZE, BLACK);
        }

        ctx.y -= (EDUCATION_INST_SIZE + 2f);

        // ── Row 2: Degree (regular) ────────────────────────────────────────
        if (hasText(degree)) {
            writeWrappedText(cs, ctx, degree,
                    LEFT_MARGIN, EDUCATION_DETAIL_SIZE, REGULAR, 1.5f);
        }
    }


    // =========================================================
    // EXPERIENCE
    // =========================================================

    private void writeExperience(
            PDPageContentStream cs,
            PageContext ctx,
            ExperienceDTO experience
    ) throws IOException {

        if (experience == null) return;
        ensureSpace(ctx, 20f);

        String role    = clean(safe(experience.getRole()));
        String company = clean(safe(experience.getCompany()));

        String heading = role;
        if (hasText(company)) {
            heading = hasText(heading) ? heading + " \u2013 " + company : company;
        }

        if (hasText(heading)) {
            drawText(cs, heading,
                    LEFT_MARGIN, ctx.y, BOLD, EDUCATION_INST_SIZE, BLACK);
            ctx.y -= (EDUCATION_INST_SIZE + 2f);
        }

        if (hasText(experience.getDuration())) {
            writeWrappedText(cs, ctx, clean(experience.getDuration()),
                    LEFT_MARGIN, EDUCATION_DETAIL_SIZE, REGULAR, 1.5f);
        }

        if (hasText(experience.getDescription())) {
            writeBullet(cs, ctx, experience.getDescription(), BULLET_SIZE);
        }
    }


    // =========================================================
    // CERTIFICATION BULLET  (bold name + regular issuer)
    //
    // Expected format from AI: "Java Full Stack Developer - Capgemini"
    // or just "Java Full Stack Developer"
    //
    // Rendered as:   •  Java Full Stack Developer – Capgemini
    //                   (bold name)                (regular)
    // =========================================================

    private void writeCertificationBullet(
            PDPageContentStream cs,
            PageContext ctx,
            String certText
    ) throws IOException {

        if (!hasText(certText)) return;

        ensureSpace(ctx, CERTIFICATION_SIZE + 4f);

        // Split on " - " or " – " to separate name from issuer
        String certName   = certText;
        String certIssuer = null;

        int dashIdx = certText.indexOf(" - ");
        if (dashIdx == -1) dashIdx = certText.indexOf(" \u2013 ");
        if (dashIdx != -1) {
            certName   = clean(certText.substring(0, dashIdx));
            certIssuer = clean(certText.substring(dashIdx).replaceFirst("^\\s*[-\u2013]\\s*", ""));
        }

        // ── Draw bullet character ──────────────────────────────────────────
        drawText(cs, "\u2022",
                BULLET_CHAR_X, ctx.y, REGULAR, CERTIFICATION_SIZE, BLACK);

        // ── Draw bold cert name ───────────────────────────────────────────
        float curX  = BULLET_TEXT_X;
        float nameW = getTextWidth(certName, BOLD, CERTIFICATION_SIZE);

        drawText(cs, certName, curX, ctx.y, BOLD, CERTIFICATION_SIZE, BLACK);
        curX += nameW;

        // ── Draw regular issuer (if present) ─────────────────────────────
        if (hasText(certIssuer)) {
            String issuerWithDash = " \u2013 " + certIssuer;
            drawText(cs, issuerWithDash, curX, ctx.y, REGULAR, CERTIFICATION_SIZE, BLACK);
        }

        ctx.y -= (CERTIFICATION_SIZE + BULLET_LEADING);
    }


    // =========================================================
    // SKILL ROW  (bold label, then comma-separated values)
    //
    // Languages:        Java, Python, SQL, JavaScript
    // =========================================================

    private void writeSkillLine(
            PDPageContentStream cs,
            PageContext ctx,
            String category,
            List<String> skills
    ) throws IOException {

        if (skills == null || skills.isEmpty()) return;

        String skillText = clean(String.join(", ", skills));
        if (!hasText(skillText)) return;

        ensureSpace(ctx, SKILLS_SIZE + 3f);

        // Fixed label-column width based on the widest label "DevOps & Tools:"
        float labelColW = getTextWidth("DevOps & Tools:", BOLD, SKILLS_SIZE) + 14f;
        float skillX    = LEFT_MARGIN + labelColW;

        drawText(cs, category, LEFT_MARGIN, ctx.y, BOLD, SKILLS_SIZE, BLACK);

        writeWrappedText(cs, ctx, skillText, skillX, SKILLS_SIZE, REGULAR, SKILL_LEADING);

        // If writeWrappedText consumed y, we may need only the after-row gap
        ctx.y -= AFTER_SKILL_ROW;
    }


    // =========================================================
    // BULLET  (• + wrapped text)
    // =========================================================

    private void writeBullet(
            PDPageContentStream cs,
            PageContext ctx,
            String text,
            float fontSize
    ) throws IOException {

        if (!hasText(text)) return;
        ensureSpace(ctx, fontSize + 3f);

        drawText(cs, "\u2022",
                BULLET_CHAR_X, ctx.y, REGULAR, fontSize, BLACK);

        writeWrappedText(cs, ctx, text,
                BULLET_TEXT_X, fontSize, REGULAR, BULLET_LEADING);
    }


    // =========================================================
    // WRAPPED TEXT  (word-wrap inside right margin)
    // =========================================================

    private void writeWrappedText(
            PDPageContentStream cs,
            PageContext ctx,
            String text,
            float x,
            float fontSize,
            PDType1Font font,
            float extraSpacing
    ) throws IOException {

        if (!hasText(text)) return;

        String cleaned = clean(text);
        String[] words = cleaned.split("\\s+");

        StringBuilder line = new StringBuilder();
        float availableW   = PAGE_WIDTH - RIGHT_MARGIN - x;

        for (String word : words) {

            String testLine = line.isEmpty() ? word : line + " " + word;
            float  testW    = getTextWidth(testLine, font, fontSize);

            if (testW > availableW && !line.isEmpty()) {
                writeLine(cs, ctx, line.toString(), x, fontSize, font, extraSpacing);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (!line.isEmpty()) {
            writeLine(cs, ctx, line.toString(), x, fontSize, font, extraSpacing);
        }
    }


    // =========================================================
    // WRITE ONE LINE
    // =========================================================

    private void writeLine(
            PDPageContentStream cs,
            PageContext ctx,
            String text,
            float x,
            float fontSize,
            PDType1Font font,
            float extraSpacing
    ) throws IOException {

        ensureSpace(ctx, fontSize + extraSpacing + 1f);

        drawText(cs, text, x, ctx.y, font, fontSize, BLACK);

        ctx.y -= (fontSize + extraSpacing);
    }


    // =========================================================
    // DRAW TEXT
    // =========================================================

    private void drawText(
            PDPageContentStream cs,
            String text,
            float x, float y,
            PDType1Font font,
            float fontSize,
            Color color
    ) throws IOException {

        if (!hasText(text)) return;

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(clean(text));
        cs.endText();
    }


    // =========================================================
    // TEXT WIDTH
    // =========================================================

    private float getTextWidth(String text, PDType1Font font, float fontSize) {
        if (!hasText(text)) return 0f;
        try {
            return font.getStringWidth(clean(text)) / 1000f * fontSize;
        } catch (IOException e) {
            return 0f;
        }
    }


    // =========================================================
    // SPACE GUARD
    // =========================================================

    private void ensureSpace(PageContext ctx, float requiredHeight) {
        if (ctx.y - requiredHeight < BOTTOM_MARGIN) {
            throw new RuntimeException(
                    "Resume content exceeds one A4 page. " +
                            "Please reduce resume content.");
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String safe(String v)    { return v == null ? "" : v; }
    private boolean hasText(String v) { return v != null && !v.trim().isEmpty(); }

    private String clean(String v) {
        if (v == null) return "";
        return v
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\u2013", "-")
                .replace("\u2014", "-")
                .replace("\u2018", "'")
                .replace("\u2019", "'")
                .replace("\u201C", "\"")
                .replace("\u201D", "\"")
                .replace("\u2022", "\u2022")
                .replaceAll("\\s+", " ")
                .trim();
    }
}