package eu.homeanthill.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

import eu.homeanthill.repository.LoginRepository

class AppAuthenticator(
    private val loginRepository: LoginRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.isUnauthorized()) {
            loginRepository.logoutAndRedirect()

            // TODO find a way to pass a reference via DI to redirect to login page
//                currentScreenHook?.onUnAuthorizedError()
        }
        return null
    }

    private fun Response.isUnauthorized() = this.code == 401

}