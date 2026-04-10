package com.devpa.app.data.repository

import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.data.db.JourneyStepEntity
import javax.inject.Inject

class SeedDataUseCase @Inject constructor(
    private val repository: JourneyRepository
) {
    suspend fun execute() {
        if (repository.journeyDao.getJourneyCount() != 0) return

        // ── Journey 1 — Game Dev Portfolio ────────────────────────
        val j1Id = repository.insertJourney(
            JourneyEntity(name = "Game Dev Portfolio", iconEmoji = "🎮", colourHex = "#7FFF6E", sortOrder = 0)
        )
        repository.setActiveJourney(j1Id)

        val j1Steps = mutableListOf<JourneyStepEntity>()
        var order = 0
        fun step(label: String, cat: String?) =
            JourneyStepEntity(journeyId = j1Id, label = label, category = cat, sortOrder = order++)

        j1Steps += step("Set up GitHub profile with bio & photo", "Profile")
        j1Steps += step("Pin 3–5 best repos to GitHub", "Profile")
        j1Steps += step("Game #1 — complete & playable", "Projects")
        j1Steps += step("Game #2 — different genre or mechanic", "Projects")
        j1Steps += step("Game #3 — shows a specific skill (AI, physics, proc-gen...)", "Projects")
        j1Steps += step("Devlog or postmortem for each game", "Writing")
        j1Steps += step("Publish at least 1 game on itch.io", "Publishing")
        j1Steps += step("Record a gameplay trailer (60–90 sec)", "Media")
        j1Steps += step("Portfolio website or polished itch.io profile page", "Portfolio")
        j1Steps += step("Game dev focused resume", "Job Search")
        j1Steps += step("Cover letter template", "Job Search")
        j1Steps += step("Apply to your first job", "Job Search")
        j1Steps += step("Connect with 5 devs or studios on LinkedIn", "Networking")
        j1Steps += step("Get 1 piece of public feedback or review", "Networking")
        j1Steps += step("Submit to a game jam", "Community")
        repository.insertAllSteps(j1Steps)

        // ── Journey 2 — Job Search Campaign ───────────────────────
        val j2Id = repository.insertJourney(
            JourneyEntity(name = "Job Search Campaign", iconEmoji = "💼", colourHex = "#60A5FA", sortOrder = 1)
        )
        order = 0
        repository.insertAllSteps(listOf(
            JourneyStepEntity(journeyId = j2Id, label = "Update resume for game dev roles", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Write cover letter template", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Set up LinkedIn profile", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Apply to 5 jobs this week", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Follow up on pending applications", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Prepare for technical interview", sortOrder = order++),
            JourneyStepEntity(journeyId = j2Id, label = "Prepare portfolio walkthrough", sortOrder = order++)
        ))

        // ── Journey 3 — Game Jam Sprint ────────────────────────────
        val j3Id = repository.insertJourney(
            JourneyEntity(name = "Game Jam Sprint", iconEmoji = "⚡", colourHex = "#FFC04A", sortOrder = 2)
        )
        order = 0
        repository.insertAllSteps(listOf(
            JourneyStepEntity(journeyId = j3Id, label = "Pick a theme and concept", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Prototype core mechanic", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Build the core game loop", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Add basic audio", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Polish and bug fix", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Write itch.io description", sortOrder = order++),
            JourneyStepEntity(journeyId = j3Id, label = "Submit before deadline", sortOrder = order++)
        ))

        // ── Journey 4 — Indie Game Launch ─────────────────────────
        val j4Id = repository.insertJourney(
            JourneyEntity(name = "Indie Game Launch", iconEmoji = "🚀", colourHex = "#4FD1C5", sortOrder = 3)
        )
        order = 0
        repository.insertAllSteps(listOf(
            JourneyStepEntity(journeyId = j4Id, label = "Finalise all game builds (Win/Mac/Web)", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Create itch.io or Steam store page", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Record gameplay trailer", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Set up social media presence", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Announce launch date", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Launch day post", sortOrder = order++),
            JourneyStepEntity(journeyId = j4Id, label = "Gather and respond to reviews", sortOrder = order++)
        ))

        // ── Journey 5 — Custom ─────────────────────────────────────
        repository.insertJourney(
            JourneyEntity(
                name = "Custom",
                iconEmoji = "✨",
                colourHex = "#888A8F",
                description = "Your own journey — add steps to get started",
                sortOrder = 4
            )
        )
    }

    /** Returns template steps for a given key — used by AddJourneyBottomSheet flow */
    suspend fun seedStepsForTemplate(journeyId: Long, templateKey: String) {
        var order = 0
        fun step(label: String, cat: String? = null) =
            JourneyStepEntity(journeyId = journeyId, label = label, category = cat, sortOrder = order++)

        val steps = when (templateKey) {
            "game_dev_portfolio" -> listOf(
                step("Set up GitHub profile with bio & photo", "Profile"),
                step("Pin 3–5 best repos to GitHub", "Profile"),
                step("Game #1 — complete & playable", "Projects"),
                step("Game #2 — different genre or mechanic", "Projects"),
                step("Game #3 — shows a specific skill (AI, physics, proc-gen...)", "Projects"),
                step("Devlog or postmortem for each game", "Writing"),
                step("Publish at least 1 game on itch.io", "Publishing"),
                step("Record a gameplay trailer (60–90 sec)", "Media"),
                step("Portfolio website or polished itch.io profile page", "Portfolio"),
                step("Game dev focused resume", "Job Search"),
                step("Cover letter template", "Job Search"),
                step("Apply to your first job", "Job Search"),
                step("Connect with 5 devs or studios on LinkedIn", "Networking"),
                step("Get 1 piece of public feedback or review", "Networking"),
                step("Submit to a game jam", "Community")
            )
            "job_search" -> listOf(
                step("Update resume for game dev roles"),
                step("Write cover letter template"),
                step("Set up LinkedIn profile"),
                step("Apply to 5 jobs this week"),
                step("Follow up on pending applications"),
                step("Prepare for technical interview"),
                step("Prepare portfolio walkthrough")
            )
            "game_jam" -> listOf(
                step("Pick a theme and concept"),
                step("Prototype core mechanic"),
                step("Build the core game loop"),
                step("Add basic audio"),
                step("Polish and bug fix"),
                step("Write itch.io description"),
                step("Submit before deadline")
            )
            "indie_launch" -> listOf(
                step("Finalise all game builds (Win/Mac/Web)"),
                step("Create itch.io or Steam store page"),
                step("Record gameplay trailer"),
                step("Set up social media presence"),
                step("Announce launch date"),
                step("Launch day post"),
                step("Gather and respond to reviews")
            )
            else -> emptyList()  // "blank" — no steps
        }
        if (steps.isNotEmpty()) repository.insertAllSteps(steps)
    }
}
