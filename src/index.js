function main() {
  console.info("Launched");

  //Loads the resource pack
  RenderManager.loadAsset("edx");

  RenderManager.textureRegistry().add("edx:blocks/iron_block");

  var turbineModel = RenderManager.modelRegistry().add("edx:metal_turbine.b3d");
  turbineModel.bindObjects(["LargeMetalBlade", "LargeMetalHub"]);
  turbineModel.bindTexture("iron_block", "edx:blocks/iron_block");

  BlockManager.createBlock(
    'test',
    'Test Block',
    'RIGHT_CLICK_BLOCK',
    function event_handler(event) {
      //Test function syntax sugar:
      event.world.addEntity = event.world.spawnEntityInWorld;
      /**
       * Spawn an entity based on an entity prefab.
       * Hide this!
       */
      spawnEntity = function(prefab) {
        var Entity = Java.type("com.learntomod.entity.wrapper.Entity");
        var entity = new Entity(event.world);
        prefab.pipe(entity);
        event.world.spawnEntityInWorld(entity);
        return entity;
      };
      var ent = spawnEntity(EntityManager.registry().get("turbine"));
      ent.setPosition(event.pos.getX() + 0.5, event.pos.getY() + 1, event.pos.getZ() + 0.5);
    }
  );

  EntityManager.create("turbine")
    .then(function(entity) {
      entity.setSize(1, 1);
      entity.update = function() {
        entity.components().put("frame", (entity.components().get("frame") + 2) % 60);
      }

      //Set Renderer
      entity.renderer
        .then(function(renderer) {
          turbineModel.setFrame(entity.components().get("frame"));
          renderer.addChild(turbineModel);
        });
      return entity;
    });
}
