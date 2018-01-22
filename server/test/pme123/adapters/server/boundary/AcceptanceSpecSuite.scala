package pme123.adapters.server.boundary

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice._

/*
 * This collects all Specs that need the Server setup to optimize testing (only one server initializing).
 * Created by pascal.mengelt on 01.11.2016.
 */
class AcceptanceSpecSuite extends PlaySpec with GuiceOneAppPerSuite {

override def nestedSuites = Vector(new AFConfigSpec
  , new AnimationFactoryGenericSpec
  , new AnimationFactorySpecificSpec
  , new AFTimingSpec
  , new AssetGenericSpec
  , new AssetSpecificSpec
  , new AssetTimingSpec
  , new UploadAssetSpec
  , new DirectoryBoundarySpec
  , new LayoutGenericSpec
  , new LayoutPlayerSpec
  , new LayoutRegionPlaylistSpec
  , new LayoutRegionSpec
  , new PlayerGenericSpec
  , new PlayerLayoutSpec
  , new PlayerShutdownTimingSpec
  , new PlayerSpecificSpec
  , new PlaylistGenericSpec
  , new PlaylistEntrySpec
  , new PlaylistLayoutRegionSpec
  , new ProfileGenericSpec
  , new ProfileAnimationFactorySpec
  , new ProfileAssetSpec
  , new ProfilePlayerSpec
  , new ProfileLayoutSpec
  , new ProfileValueSpec
  , new SiteGenericSpec
  , new TimingGenericSpec
  , new TimingEntrySpec
  , new UserGenericSpec
  , new UserRoleSpec
  , new RoleGenericSpec
  , new RoleRightSpec
  , new DataRepositorySpec)

// Override app if you need an Application with other than non-default parameters.
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map("ehcacheplugin" -> "disabled")).build()

}
