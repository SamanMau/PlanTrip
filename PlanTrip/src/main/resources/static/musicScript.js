let selectedGenre = null;

// 1) Hämta genre när sidan laddas
document.addEventListener("DOMContentLoaded", fetchGenres);

function fetchGenres() {
  fetch("http://127.0.0.1:8080/api/fetch-genre")
    .then(r => {
      if (!r.ok) throw new Error("Failed to fetch genres");
      return r.json();
    })
      .then(data => {
          const genre = data.genre; // <-- En enda sträng
          getRecomendations(genre); // Hämta rekommendationer direkt för den genren
      })
    .catch(err => console.error(err));
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
        const [name, url, img] = String(item).split(" | ");

        const card = document.createElement("div");
        card.className = "music-card";

        // Bild
        if (img && img.startsWith("http")) {
          const image = document.createElement("img");
          image.src = img;
          image.alt = name || "playlist cover";
          card.appendChild(image);
        }

        // Titel
        const title = document.createElement("div");
        title.className = "music-card-title";
        title.textContent = name || "Unknown title";
        card.appendChild(title);

        // Spotify-knapp
        if (url && url.startsWith("http")) {
          const link = document.createElement("a");
          link.href = url;
          link.target = "_blank";
          link.rel = "noopener";
          link.textContent = "Open in Spotify";
          card.appendChild(link);
        }

        resultDiv.appendChild(card);
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

