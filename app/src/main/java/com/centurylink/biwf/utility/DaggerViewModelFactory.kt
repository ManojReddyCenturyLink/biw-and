package com.centurylink.biwf.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
class DaggerViewModelFactory @Inject constructor(private val viewModelsMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = viewModelsMap[modelClass] ?: viewModelsMap.asIterable().firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("unknown model class $modelClass")
        return try {
            creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

/**
 * Returns a [ViewModel] of type [T].
 *
 * When necessary, a brand new ViewModel of type [T] needs to be created. If this happens, the [newViewModel]
 * lambda will be called. This lambda should return a brand new ViewModel of type [T].
 */
inline fun <reified T : ViewModel> ViewModelStoreOwner.getViewModel(crossinline newViewModel: () -> T): T {
    return getViewModel(viewModelFactory(newViewModel))
}

/**
 * Returns a [ViewModel] of type [T] which will be provided by the [factory].
 */
inline fun <reified T : ViewModel> ViewModelStoreOwner.getViewModel(factory: ViewModelProvider.Factory): T {
    val provider = ViewModelProvider(this, factory)
    return provider[T::class.java]
}

/**
 * Returns a [ViewModelProvider.Factory] for ViewModels of type [T].
 *
 * When a brand new ViewModel of type [T] needs to be created, the [newViewModel] will be called and
 * its result is the new ViewModel.
 */
inline fun <reified T : ViewModel> viewModelFactory(crossinline newViewModel: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            require(modelClass == T::class.java) {
                "Incorrect ViewModel Class. Expected ${T::class.java} but got $modelClass"
            }
            @Suppress("UNCHECKED_CAST")
            return newViewModel() as V
        }
    }

/**
 * Allows a delayed configuration of a [ViewModelProvider.Factory] with input of type [I].
 */
interface ViewModelFactoryWithInput<in I> {
    /**
     * Returns a [ViewModelProvider.Factory] that is configured for the given [input].
     */
    fun withInput(input: I): ViewModelProvider.Factory
}
