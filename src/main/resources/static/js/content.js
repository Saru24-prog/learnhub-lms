// Weekly course content (sections + files + links)
// Used by admin.html, lecturer.html and student.html
// Admin / lecturer see edit buttons (canManage), students only view & download.
let contentCourseId = null;
let contentSections = [];
let materialMap = {};

const MAX_FILE_MB = 20;

const fmtSize = b => !b ? "" : b < 1024 ? b + " B" : b < 1048576 ? (b / 1024).toFixed(0) + " KB" : (b / 1048576).toFixed(1) + " MB";

function fileIcon(name) {
  const ext = (name || "").split(".").pop().toLowerCase();
  if (ext === "pdf") return "📕";
  if (["ppt", "pptx"].includes(ext)) return "📊";
  if (["doc", "docx"].includes(ext)) return "📘";
  if (["xls", "xlsx", "csv"].includes(ext)) return "📗";
  if (["zip", "rar", "7z"].includes(ext)) return "🗜️";
  if (["png", "jpg", "jpeg", "gif", "webp"].includes(ext)) return "🖼️";
  if (["mp4", "mov", "avi", "mkv"].includes(ext)) return "🎬";
  return "📄";
}

// Loads the content into <div id="content"></div> and returns the data
async function renderContent(courseId) {
  contentCourseId = courseId;
  const data = await api(`/api/courses/${courseId}/content`);
  contentSections = data.sections;
  materialMap = {};
  data.sections.forEach(s => s.materials.forEach(m => materialMap[m.id] = m));
  const manage = data.canManage;

  $("content").innerHTML = `
    <div class="page-head" style="margin-bottom:16px">
      <h2 style="font-size:20px">Course Content</h2>
      ${manage ? `<button class="btn btn-primary btn-sm" onclick="sectionForm()">+ Add Week / Section</button>` : ""}
    </div>
    ${data.sections.length
      ? data.sections.map(s => sectionHtml(s, manage)).join("")
      : `<div class="panel" style="margin-bottom:24px"><div class="empty">${manage
          ? "No content yet. Click <b>+ Add Week / Section</b> to add Week 1."
          : "Your lecturer hasn't added any content yet."}</div></div>`}`;
  return data;
}

function refreshContent() {
  return renderContent(contentCourseId).catch(e => toast(e.message || "Failed to load content", "error"));
}

function sectionHtml(s, manage) {
  return `
    <div class="section">
      <div class="section-head">
        <div>
          <h3>${esc(s.title)}</h3>
          ${s.description ? `<p>${esc(s.description)}</p>` : ""}
        </div>
        ${manage ? `
          <div class="actions">
            <button class="btn btn-light btn-sm" onclick="fileForm(${s.id})">+ File</button>
            <button class="btn btn-light btn-sm" onclick="linkForm(${s.id})">+ Link</button>
            <button class="btn btn-light btn-sm" onclick="sectionForm(${s.id})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteSection(${s.id})">Delete</button>
          </div>` : ""}
      </div>
      ${s.materials.length
        ? `<div class="materials">${s.materials.map(m => materialHtml(m, manage)).join("")}</div>`
        : `<div class="section-empty">No materials in this section yet.</div>`}
    </div>`;
}

function materialHtml(m, manage) {
  const isLink = m.type === "LINK";
  const icon = isLink ? "🔗" : fileIcon(m.fileName);
  const meta = isLink ? esc(m.url) : `${esc(m.fileName)} · ${fmtSize(m.fileSize)}`;
  const openBtn = isLink
    ? `<a class="btn btn-light btn-sm" href="${esc(m.url)}" target="_blank" rel="noopener">Open ↗</a>`
    : `<button class="btn btn-light btn-sm" onclick="downloadMaterial(${m.id})">⬇ Download</button>`;
  return `
    <div class="material">
      <div class="icon">${icon}</div>
      <div class="info"><b>${esc(m.title)}</b><small>${meta}</small></div>
      <div class="actions">
        ${openBtn}
        ${manage ? `<button class="btn btn-danger btn-sm" onclick="deleteMaterial(${m.id})">✕</button>` : ""}
      </div>
    </div>`;
}

// ---------- Sections ----------
function sectionForm(id) {
  const s = contentSections.find(x => x.id === id) || { title: `Week ${contentSections.length + 1}` };
  openModal(id ? "Edit Section" : "Add Week / Section", `
      ${field("title", "Title *", { value: s.title, placeholder: "Week 1 - Introduction" })}
      ${field("description", "Description", { value: s.description, textarea: true, placeholder: "What this week covers..." })}`,
    async () => {
      const body = JSON.stringify({ title: val("title"), description: val("description") });
      if (id) await api(`/api/sections/${id}`, { method: "PUT", body });
      else await api(`/api/courses/${contentCourseId}/sections`, { method: "POST", body });
      toast(id ? "Section updated" : "Section added");
      refreshContent();
    },
    id ? "Save changes" : "Add section");
}

async function deleteSection(id) {
  if (!confirm("Delete this section and ALL its files and links?")) return;
  try {
    await api(`/api/sections/${id}`, { method: "DELETE" });
    toast("Section deleted");
    refreshContent();
  } catch (e) { toast(e.message || "Delete failed", "error"); }
}

// ---------- Files ----------
function fileForm(sectionId) {
  openModal("Upload File", `
      ${field("title", "Title (optional)", { placeholder: "Lecture 1 slides" })}
      <div class="field">
        <label>File * (PDF, slides, Word, zip... max ${MAX_FILE_MB}MB)</label>
        <input id="file" type="file">
        <div class="err" id="fileErr"></div>
      </div>`,
    async () => {
      const file = $("file").files[0];
      if (!file) throw { errors: { file: "Please choose a file" } };
      if (file.size > MAX_FILE_MB * 1024 * 1024) throw { errors: { file: `File is too large (max ${MAX_FILE_MB}MB)` } };
      const form = new FormData();
      form.append("title", val("title"));
      form.append("file", file);
      await api(`/api/sections/${sectionId}/files`, { method: "POST", body: form });
      toast("File uploaded");
      refreshContent();
    },
    "Upload");
}

// ---------- Links ----------
function linkForm(sectionId) {
  openModal("Add Link", `
      ${field("title", "Title *", { placeholder: "Lecture 1 recording" })}
      ${field("url", "Link *", { placeholder: "https://youtube.com/..." })}`,
    async () => {
      await api(`/api/sections/${sectionId}/links`, {
        method: "POST",
        body: JSON.stringify({ title: val("title"), url: val("url") })
      });
      toast("Link added");
      refreshContent();
    },
    "Add link");
}

async function deleteMaterial(id) {
  if (!confirm("Delete this material?")) return;
  try {
    await api(`/api/materials/${id}`, { method: "DELETE" });
    toast("Material deleted");
    refreshContent();
  } catch (e) { toast(e.message || "Delete failed", "error"); }
}

async function downloadMaterial(id) {
  const m = materialMap[id];
  try {
    await downloadFile(`/api/materials/${id}/download`, m ? m.fileName : "download");
  } catch (e) { toast(e.message || "Download failed", "error"); }
}
