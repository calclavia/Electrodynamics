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
      var ent = EntityManager.instantiate(event.world, "turbine");
      ent.setPosition(event.pos.getX() + 0.5, event.pos.getY() + 1, event.pos.getZ() + 0.5);
    }
  );

  EntityManager.create("turbine")
    .then(function(entity) {
      entity.setSize(1, 1);

      //Update event
      entity
        .on("Update")
        .bind(function() {
          entity.components().put("frame", (entity.components().get("frame") + 2) % 60);
        });

      //Renderer event
      entity
        .on("Render")
        .bind(function(evt) {
          evt.pipeline
            .then(function(renderer) {
              turbineModel.setFrame(entity.components().get("frame"));
              renderer.addChild(turbineModel);
            });
        });
      return entity;
    });
}
