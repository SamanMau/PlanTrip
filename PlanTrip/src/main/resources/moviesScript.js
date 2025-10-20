document.addEventListener("DOMContentLoaded", () => {
  const dropdown = document.getElementById("genreDropdown");
  const resultContainer = document.getElementById("movieResult");

  dropdown.addEventListener("change", async () => {
    const genre = dropdown.value;
    if (!genre) {
      resultContainer.innerHTML = `<p class="placeholder">Choose a genre above to see movie recommendations...</p>`;
      return;
    }

    try {
      // 🔹 Hämta filmer från Spring Boot API
      const response = await fetch(`/api/getMovies?genre=${encodeURIComponent(genre)}`);
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

    // 🔹 Skapa filmkort för varje film
    resultContainer.innerHTML = ""; // rensa tidigare innehåll

    movies.forEach(movieString => {
      const [backdropPath, title, overview] = movieString.split(" | ");

      // Bygg kort
      const card = document.createElement("div");
      card.classList.add("movie-card");

      // Lägg till bild, titel och beskrivning
      const img = document.createElement("img");
      img.src = backdropPath && backdropPath.trim() !== "" ? backdropPath : "pictures/default-movie.jpg";
      img.alt = title || "Movie Poster";

      const infoDiv = document.createElement("div");
      infoDiv.classList.add("movie-info");

      const titleElem = document.createElement("h3");
      titleElem.textContent = title || "Unknown Title";

      const overviewElem = document.createElement("p");
      overviewElem.textContent = overview || "No description available.";

      infoDiv.appendChild(titleElem);
      infoDiv.appendChild(overviewElem);

      card.appendChild(img);
      card.appendChild(infoDiv);

      resultContainer.appendChild(card);
    });
  }
});
