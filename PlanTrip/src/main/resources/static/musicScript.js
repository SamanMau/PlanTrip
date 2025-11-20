let selectedGenre = null;

// 1) Hämta tre genrer när sidan laddas
document.addEventListener("DOMContentLoaded", fetchGenres);

function fetchGenres() {
  fetch("http://127.0.0.1:8080/api/fetch-genre")
    .then(r => {
      if (!r.ok) throw new Error("Failed to fetch genres");
      return r.json();
    })
    .then(genres => {
      ["genreBtn1"].forEach((id, i) => {
        const btn = document.getElementById(id); 
        if (btn && genres[i]) {
          btn.querySelector(".label").textContent = genres[i];
          btn.dataset.genre = genres[i];
          selectedGenre = genres[i];
          btn.disabled = false;
          btn.addEventListener("click", () => getRecomendations(genres[i]));
        }
      });
    })
    .catch(err => console.error(err));
}

// 2) Klick på en genre-knapp → hämta rekommendationer för just den genren
function wireGenreClicks() {
  document.querySelectorAll(".genre-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const chosen = btn.dataset.genre;
      selectedGenre = chosen;
      getRecomendations(selectedGenre); // OBS: nu skickar vi vald genre
    });
  });
}

// 3) Hämta rekommendationer för en vald genre
// 3) Hämta rekommendationer för en vald genre (List<String> som JSON-array)
// 3) Hämta rekommendationer för en vald genre (List<String> som JSON-array)
function getRecomendations(genre) {
  const url = `http://127.0.0.1:8080/api/musicRecommendations?genre=${encodeURIComponent(genre)}`;

  fetch(url)
    .then((response) => {
      if (!response.ok) throw new Error("Failed to fetch music data");
      return response.json(); // <-- förväntar sig t.ex. ["Name | https://... | https://img...", "..."]
    })
    .then((data) => {
      const resultDiv = document.getElementById("musicResult");
      resultDiv.innerHTML = ""; // rensa tidigare resultat

      if (!Array.isArray(data)) {
        resultDiv.textContent = "Oväntat svarformat (förväntade en lista av strängar).";
        return;
      }

      // Visa varje sträng på egen rad
      for (const item of data) {
        // Om du bygger strängarna som "name | url | img", splitta:
        const [name, url, img] = String(item).split(" | ");

        const p = document.createElement("p");

        // Namn
        if (name) {
          const strong = document.createElement("strong");
          strong.textContent = name;
          p.appendChild(strong);
        }

        // Länk (om finns)
        if (url && url.startsWith("http")) {
          p.appendChild(document.createTextNode(" – "));
          const a = document.createElement("a");
          a.href = url;
          a.target = "_blank";
          a.rel = "noopener";
          a.textContent = "Öppna";
          p.appendChild(a);
        }

        // Bild (om finns)
        if (img && img.startsWith("http")) {
          p.appendChild(document.createTextNode(" "));
          const image = document.createElement("img");
          image.src = img;
          image.alt = name || "playlist cover";
          image.style.maxHeight = "36px";
          image.style.marginLeft = "8px";
          p.appendChild(image);
        }

        // Om strängen inte följer "name | url | img", visa rakt av:
        if (!name && !url && !img) {
          p.textContent = String(item);
        }

        resultDiv.appendChild(p);
      }
    })
    .catch((err) => console.error("Error:", err));
}

      function openSpotifyModal() {
      document.getElementById("spotifyModal").style.display = "block";
    }

    function closeSpotifyModal() {
      document.getElementById("spotifyModal").style.display = "none";
    }

    function authenticateSpotify() {
      const spotifyUrl = "https://accounts.spotify.com/authorize?client_id=72a9d8f2ee974b11b040c55d4319f934&response_type=code&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fapi%2Fcallback&scope=playlist-modify-public%20user-read-private&show_dialog=true";
      window.open(spotifyUrl, "_blank");
    }

