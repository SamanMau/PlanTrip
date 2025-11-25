document.addEventListener("DOMContentLoaded", getActivities);

function getActivities() {
  fetch("http://127.0.0.1:8080/api/fetch-activities")
    .then(r => {
      if (!r.ok) throw new Error("Failed to fetch genres");
      return r.json();
    })
      .then(data => {
          const genre = data.genre; // <-- En enda sträng
      })
    .catch(err => console.error(err));
}
