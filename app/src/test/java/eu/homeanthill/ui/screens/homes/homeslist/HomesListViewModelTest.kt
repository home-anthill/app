package eu.homeanthill.ui.screens.homes.homeslist

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.repository.HomesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HomesListViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockHomesRepo = mockk<HomesRepository>()

    private val testHome = Home(
        id = "home1",
        name = "Test Home",
        location = "Test City",
        rooms = null,
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val okMessage = GenericMessageResponse(message = "ok")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // --- init ---

    @Test
    fun `init emits Idle with homes list on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Idle)
        assertEquals(listOf(testHome), (state as HomesListViewModel.HomesUiState.Idle).homes)
        coVerify(exactly = 1) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `init emits Error when repoGetHomes throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } throws IOException("Network error")

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Error)
        assertEquals("Network error", (state as HomesListViewModel.HomesUiState.Error).errorMessage)
    }

    // --- createHome ---

    @Test
    fun `createHome calls postHome then refreshes homes list on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoPostHome(any()) } returns testHome

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.createHome("New Home", "New City")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Idle)
        // postHome called once, getHomes called twice (init + after create)
        coVerify(exactly = 1) { mockHomesRepo.repoPostHome(any()) }
        coVerify(exactly = 2) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `createHome emits Error when repoPostHome throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoPostHome(any()) } throws IOException("Create failed")

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.createHome("New Home", "New City")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Error)
        assertEquals("Create failed", (state as HomesListViewModel.HomesUiState.Error).errorMessage)
    }

    // --- editHome ---

    @Test
    fun `editHome calls putHome then refreshes homes list on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoPutHome(any(), any()) } returns okMessage

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.editHome("home1", "Updated Home", "Updated City")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Idle)
        coVerify(exactly = 1) { mockHomesRepo.repoPutHome(any(), any()) }
        coVerify(exactly = 2) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `editHome emits Error when repoPutHome throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoPutHome(any(), any()) } throws IOException("Update failed")

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.editHome("home1", "Updated Home", "Updated City")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Error)
        assertEquals("Update failed", (state as HomesListViewModel.HomesUiState.Error).errorMessage)
    }

    // --- deleteHome ---

    @Test
    fun `deleteHome calls repoDeleteHome then refreshes homes list on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoDeleteHome(any()) } returns okMessage

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.deleteHome("home1")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Idle)
        coVerify(exactly = 1) { mockHomesRepo.repoDeleteHome("home1") }
        coVerify(exactly = 2) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `deleteHome emits Error when repoDeleteHome throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockHomesRepo.repoDeleteHome(any()) } throws IOException("Delete failed")

        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()

        vm.deleteHome("home1")
        advanceUntilIdle()

        val state = vm.homesUiState.value
        assertTrue(state is HomesListViewModel.HomesUiState.Error)
        assertEquals("Delete failed", (state as HomesListViewModel.HomesUiState.Error).errorMessage)
    }
}
