# ikt205_oblig

# 📚 Applikasjons Avokadoene

En Android-app for administrasjon av kurs og studenter. Appen lar lærere holde oversikt over studenter, kurs og karakterer.

## 🏗️ Prosjektstruktur

Prosjektet er organisert som følger:

- **`activities/`** – Inneholder alle skjermene (UI og logikk) i appen.
  - `MainActivity.kt`: Hovedskjermen med navigasjon til kurs- og studentadministrasjon.
  - `CourseListActivity.kt`: Skjerm for å vise, legge til, redigere og slette kurs.
  - `StudentListActivity.kt`: Skjerm for å vise, legge til, redigere og slette studenter.
  - `AddEditCourseActivity.kt`: Skjerm for å legge til og redigere kurs.
  - `AddEditStudentActivity.kt`: Skjerm for å legge til og redigere studenter.

- **`adapters/`** – Adaptere for RecyclerView, brukes for å vise lister over kurs og studenter.
  - `CourseAdapter.kt`: Adapter for kurser, håndterer RecyclerView.
  - `StudentAdapter.kt`: Adapter for studenter, håndterer RecyclerView.

- **`models/`** – Definerer datamodeller for appen.
  - `Course.kt`: Modell for kurs, inkluderer kurskode, navn, instruktør og antall studenter.
  - `Student.kt`: Modell for studenter, inkluderer navn, student-ID og e-post.

- **`utils/`** – Hjelpefunksjoner.
  - `FirebaseUtil.kt`: Hjelpeklasse for Firestore-integrasjon.

- **`res/layout/`** – XML-filer som definerer brukergrensesnittet.
  - `activity_main.xml`: Layout for hovedskjermen.
  - `activity_course_list.xml`: Layout for kurslisten.
  - `activity_student_list.xml`: Layout for studentlisten.
  - `activity_add_edit_course.xml`: Layout for å legge til/redigere kurs.
  - `activity_add_edit_student.xml`: Layout for å legge til/redigere studenter.
  - `item_course.xml`: Layout for et kurs i RecyclerView.
  - `item_student.xml`: Layout for en student i RecyclerView.

## 🚀 Kom i gang

### 1️⃣ Installer avhengigheter
For å kjøre prosjektet, må du ha:
- **Android Studio** (siste versjon)
- **Firebase konfigurasjon** (sørg for at `google-services.json` er lagt til i `app/`-mappen)

### 2️⃣ Klon repoet og åpne det i Android Studio
```sh
  git clone https://github.com/dittrepo/applikasjons_avokadoene.git
  cd applikasjons_avokadoene
  ```

### 3️⃣ Kjør prosjektet
1. Åpne prosjektet i Android Studio
2. Kjør `Sync Project with Gradle Files`
3. Bygg og kjør appen på en emulator eller fysisk enhet

## 📌 Viktige komponenter

### `MainActivity.kt`
- Hovedskjermen som navigerer til kurs- og studentadministrasjon.
- Bruker `Intent` for å åpne `CourseListActivity` og `StudentListActivity`.

### `CourseListActivity.kt`
- Viser en liste over kurs.
- Bruker `CourseAdapter.kt` for å håndtere RecyclerView.
- Implementerer funksjonalitet for å legge til, redigere og slette kurs.
- Henter data fra Firestore via `FirebaseUtil.kt`.

### `StudentListActivity.kt`
- Viser en liste over studenter.
- Bruker `StudentAdapter.kt` for å vise studentinformasjon i RecyclerView.
- Henter data fra Firestore via `FirebaseUtil.kt`.

### `CourseAdapter.kt` og `StudentAdapter.kt`
- Håndterer oppdatering av lister i RecyclerView.
- `onBindViewHolder()` oppdaterer visningen med kurs- eller studentdata.
- Lyttere for redigering og sletting av elementer.

### `FirebaseUtil.kt`
- Inneholder funksjoner for å hente, oppdatere og slette data fra Firestore.
- `getCoursesCollection()`: Referanse til kurssamlingen.
- `getStudentsCollection()`: Referanse til studentsamlingen.

### `AddEditCourseActivity.kt`
- Skjerm for å legge til/redigere kurs.
- Inneholder `EditText`-felter for kursnavn, kurskode, instruktør og antall studenter.
- Lagrer kurs til Firestore ved å kalle `FirebaseUtil.getCoursesCollection().add(course)`.

### `AddEditStudentActivity.kt`
- Skjerm for å legge til/redigere studenter.
- Inneholder `EditText`-felter for studentnavn, student-ID og e-post.
- Lagrer student til Firestore via `FirebaseUtil.getStudentsCollection().add(student)`.

## 🛠️ Brukte teknologier
- **Kotlin** – Programmeringsspråk
- **Android Jetpack** – Moderne Android-utvikling
- **RecyclerView** – Vise lister over kurs og studenter
- **Firebase Firestore** – Database for lagring av data


