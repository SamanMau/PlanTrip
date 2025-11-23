document.addEventListener("DOMContentLoaded", () => {
  const dropdown = document.getElementById("genreDropdown");
  const resultContainer = document.getElementById("movieResult");
  resultContainer.innerHTML = `
    <p class="placeholder">Choose a genre above to see movie recommendations...</p>
`;

  dropdown.addEventListener("change", async () => {
    const genre = dropdown.value;
    if (!genre) {
      resultContainer.innerHTML = `<p class="placeholder">Choose a genre above to see movie recommendations...</p>`;
      return;
    }

    try {
      // 🔹 Hämta filmer från Spring Boot API
      const response = await fetch(`http://127.0.0.1:8080/api/getMovies?genre=${encodeURIComponent(genre)}`);
      if (!response.ok) throw new Error("Failed to fetch movies");

      const movies = await response.json(); // förväntar sig en lista av strängar
      renderMovies(movies);

    } catch (error) {
      console.error("Error fetching movies:", error);
      resultContainer.innerHTML = `<p class="placeholder">Something went wrong while fetching movies 😔</p>`;
    }
  });

  function renderMovies(movies) {
    if (!movies || movies.length === 0) {
      resultContainer.innerHTML = `<p class="placeholder">No movies found for this genre.</p>`;
      return;
    }

    resultContainer.innerHTML = ""; // töm tidigare resultat

    movies.forEach(movieString => {

      // 1️⃣ Splitta upp backend-strängen
      const parts = movieString.split(" | ");

      const title = parts[0] || "Unknown title";
      const description = parts[1] || "No description";
      const posterUrl = parts[2] || "pictures/default-movie.jpg";

      // Eftersom backend skickar t.ex. "Release Date 2023-06-01"
      const releaseDate = parts[3]?.replace("Release Date", "").trim() || "Unknown";

      // Backend skickar "Language en"
      const language = parts[4]?.replace("Language", "").trim() || "Unknown";

      // Backend skickar "Genres Action, Thriller"
      const genres = parts[5]?.replace("Genres", "").trim() || "Unknown";

      // 2️⃣ Bygg filmkortet
      const card = document.createElement("div");
      card.classList.add("movie-card");

      card.innerHTML = `
        <div class="movie-poster">
          <img src="${posterUrl}" alt="${title}">
        </div>

        <div class="movie-main">
          <h3 class="movie-title">${title}</h3>
          <p class="movie-genres">Genres: ${genres}</p>
          <p class="movie-description">${description}</p>
        </div>

        <div class="movie-meta">
          <span class="movie-release">Release date: ${releaseDate}</span>
          <span class="movie-language">Language: ${language}</span>
        </div>
      `;

      resultContainer.appendChild(card);
    });
  }

});
