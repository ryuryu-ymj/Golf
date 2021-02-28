package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.body
import ktx.box2d.circle

const val BALL_SIZE = 0.05f
private const val NORMAL_DAMPING = 0.1f
//private const val LARGE_DAMPING = 0.4f

class Ball(asset: AssetManager, world: World, x: Float, y: Float) : Actor() {
    val body: Body
    private val texture = asset.get<Texture>("image/ball.png")
    private var lastSpeed = 0f
    private var decelerateCount = 0
    private var contactCount = 0

    init {
        setPosition(x, y)
        setSize(BALL_SIZE, BALL_SIZE)
        setOrigin(width / 2, height / 2)
        body = world.body {
            circle(radius = width / 2) {
                density = 20f
                restitution = 0.7f
                friction = 0.5f
            }
            type = BodyDef.BodyType.DynamicBody
            linearDamping = NORMAL_DAMPING
            //angularDamping = 0.2f
            position.set(x + originX, y + originY)
        }
        //println("${body.mass * 1000} g")
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                if (body == contact.fixtureA.body) {
                    contactCount++
                    if (contactCount == 1) {
                        contact.fixtureB.body.userData.let {
                            if (it is GroundType) {
                                body.linearDamping = it.linearDamping
                            }
                        }
                    }
                } else if (body == contact.fixtureB.body) {
                    contactCount++
                    if (contactCount == 1) {
                        contact.fixtureA.body.userData.let {
                            if (it is GroundType) {
                                body.linearDamping = it.linearDamping
                            }
                        }
                    }
                }
            }

            override fun preSolve(contact: Contact, oldManifold: Manifold) {
            }

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {
            }

            override fun endContact(contact: Contact) {
                if (body == contact.fixtureA.body ||
                    body == contact.fixtureB.body) {
                    contactCount--
                    if (contactCount == 0) {
                        body.linearDamping = NORMAL_DAMPING
                    }
                }
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)
        body.position.let { setPosition(it.x - originX, it.y - originY) }
        rotation = body.angle * MathUtils.radiansToDegrees

        val speed = body.linearVelocity.len()
        //println(speed)
        if (speed > lastSpeed) {
            if (decelerateCount > 0) {
                decelerateCount = 0
                //body.linearDamping = NORMAL_DAMPING
            }
        } else if (speed < 0.1f) {
            decelerateCount++
            //body.linearDamping = LARGE_DAMPING
            if (decelerateCount > 60) {
                body.isAwake = false
            }
        }
        lastSpeed = speed
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            texture, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation,
            0, 0, texture.width, texture.height, false, false
        )
    }
}