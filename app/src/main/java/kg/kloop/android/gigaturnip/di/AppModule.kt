package kg.kloop.android.gigaturnip.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.kloop.android.gigaturnip.data.models.mappers.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.util.Constants.API_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesGigaTurnipRepository(
        api: GigaTurnipApi
    ) = GigaTurnipRepository(
        api,
        CampaignDtoMapper(),
        TaskDtoMapper(),
        TaskStageDtoMapper()
    )

    @Singleton
    @Provides
    fun providesGigaTurnipApi(): GigaTurnipApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(API_BASE_URL)
            .build()
            .create(GigaTurnipApi::class.java)

    }

    @Singleton
    @Provides
    fun provideCampaignDtoMapper(): CampaignDtoMapper {
        return CampaignDtoMapper()
    }

    @Singleton
    @Provides
    fun provideTaskDtoMapper(): TaskDtoMapper {
        return TaskDtoMapper()
    }

    @Singleton
    @Provides
    fun provideTaskStageDtoMapper(): TaskStageDtoMapper {
        return TaskStageDtoMapper()
    }

}