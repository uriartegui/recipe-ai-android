package com.guiuriarte.recipeai.di

import com.guiuriarte.recipeai.BuildConfig
import com.guiuriarte.recipeai.data.api.AiService
import com.guiuriarte.recipeai.data.api.UnsplashService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import com.guiuriarte.recipeai.data.api.MealDbService

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(@Named("ai") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAiService(@Named("ai") retrofit: Retrofit): AiService {
        return retrofit.create(AiService::class.java)
    }

    @Provides
    @Singleton
    @Named("unsplash")
    fun provideUnsplashRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUnsplashService(@Named("unsplash") retrofit: Retrofit): UnsplashService {
        return retrofit.create(UnsplashService::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("recipe_ai_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    @Named("mealdb")
    fun provideMealDbRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMealDbService(@Named("mealdb") retrofit: Retrofit): MealDbService {
        return retrofit.create(MealDbService::class.java)
    }
}
