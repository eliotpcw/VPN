package kz.kazpost.vpn.di

import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import kotlin.coroutines.CoroutineContext

open class CoroutineProvider : KoinComponent {
    open val Main: CoroutineContext by inject(named(NAME_MAIN))
    open val IO: CoroutineContext by inject(named(NAME_IO))
}