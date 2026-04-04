package eu.homeanthill.ui.screens.devices.editdevice

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
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class EditDeviceViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockHomesRepo = mockk<HomesRepository>()
    private val mockDevicesRepo = mockk<DevicesRepository>()

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

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        val state = vm.editDeviceUiState.value
        assertTrue(state is EditDeviceViewModel.EditDeviceUiState.Idle)
        assertEquals(listOf(testHome), (state as EditDeviceViewModel.EditDeviceUiState.Idle).homes)
        coVerify(exactly = 1) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `init emits Error when repoGetHomes throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } throws IOException("Load homes failed")

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        val state = vm.editDeviceUiState.value
        assertTrue(state is EditDeviceViewModel.EditDeviceUiState.Error)
        assertEquals("Load homes failed", (state as EditDeviceViewModel.EditDeviceUiState.Error).errorMessage)
    }

    // --- assignDevice ---

    @Test
    fun `assignDevice calls repoAssignDeviceToHomeRoom then refreshes homes on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockDevicesRepo.repoAssignDeviceToHomeRoom(any(), any()) } returns okMessage

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        vm.assignDevice("dev1", "home1", "room1")
        advanceUntilIdle()

        val state = vm.editDeviceUiState.value
        assertTrue(state is EditDeviceViewModel.EditDeviceUiState.Idle)
        coVerify(exactly = 1) { mockDevicesRepo.repoAssignDeviceToHomeRoom(any(), any()) }
        coVerify(exactly = 2) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `assignDevice emits Error when repoAssignDeviceToHomeRoom throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockDevicesRepo.repoAssignDeviceToHomeRoom(any(), any()) } throws IOException("Assign failed")

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        vm.assignDevice("dev1", "home1", "room1")
        advanceUntilIdle()

        val state = vm.editDeviceUiState.value
        assertTrue(state is EditDeviceViewModel.EditDeviceUiState.Error)
        assertEquals("Assign failed", (state as EditDeviceViewModel.EditDeviceUiState.Error).errorMessage)
    }

    // --- deleteDevice ---

    @Test
    fun `deleteDevice calls repoDeleteDevice on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockDevicesRepo.repoDeleteDevice("dev1") } returns okMessage

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        vm.deleteDevice("dev1")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockDevicesRepo.repoDeleteDevice("dev1") }
    }

    @Test
    fun `deleteDevice emits Error when repoDeleteDevice throws IOException`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockDevicesRepo.repoDeleteDevice("dev1") } throws IOException("Delete failed")

        val vm = EditDeviceViewModel(mockHomesRepo, mockDevicesRepo)
        advanceUntilIdle()

        vm.deleteDevice("dev1")
        advanceUntilIdle()

        val state = vm.editDeviceUiState.value
        assertTrue(state is EditDeviceViewModel.EditDeviceUiState.Error)
        assertEquals("Delete failed", (state as EditDeviceViewModel.EditDeviceUiState.Error).errorMessage)
    }
}
